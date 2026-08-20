package com.zeropick.backend.comparison.service;

import com.zeropick.backend.comparison.dto.ComparisonBoxToggleResponse;
import com.zeropick.backend.comparison.dto.ProductCompareResponse;
import com.zeropick.backend.comparison.entity.ComparisonBox;
import com.zeropick.backend.comparison.exception.ComparisonCategoryMismatchException;
import com.zeropick.backend.comparison.repository.ComparisonBoxRepository;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.repository.ProductRepository;
import com.zeropick.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComparisonService {

    private final ComparisonBoxRepository comparisonBoxRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // 1. 비교함 상품 토글 (담기 / 빼기)
    @Transactional
    public ComparisonBoxToggleResponse toggleComparisonBox(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        Optional<ComparisonBox> existingBox = comparisonBoxRepository.findByUserIdAndProductId(userId, productId);

        if (existingBox.isPresent()) {
            comparisonBoxRepository.delete(existingBox.get());
            return new ComparisonBoxToggleResponse(productId, false);
        }

        Product newProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        List<ComparisonBox> userBoxes = comparisonBoxRepository.findByUserId(userId);
        if (!userBoxes.isEmpty()) {
            Long existingProductId = userBoxes.get(0).getProductId();
            Product existingProduct = productRepository.findById(existingProductId).orElse(null);

            if (existingProduct != null && !existingProduct.getCategoryId().equals(newProduct.getCategoryId())) {
                throw new ComparisonCategoryMismatchException();
            }
        }

        comparisonBoxRepository.save(new ComparisonBox(userId, productId));
        return new ComparisonBoxToggleResponse(productId, true);
    }

    // 2. 비교함 상품 삭제 (26번 API - 이미 존재하지 않는 경우에도 200 반환하도록 먹등 처리)
    @Transactional
    public ComparisonBoxToggleResponse deleteComparisonBoxProduct(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        // 비교함에 존재하는 경우 삭제 진행 (없어도 에러를 던지지 않고 200 응답 유지)
        comparisonBoxRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(comparisonBoxRepository::delete);

        return new ComparisonBoxToggleResponse(productId, false);
    }

    // 3. 내 비교함 목록 조회 API
    public ProductCompareResponse getComparisonTable(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        List<ComparisonBox> boxes = comparisonBoxRepository.findByUserId(userId);

        List<ProductCompareResponse.ComparisonItem> items = boxes.stream().map(box -> {
            Product product = productRepository.findById(box.getProductId()).orElse(null);
            if (product == null) return null;

            Integer score = (product.getScore() != null) ? product.getScore().intValue() : 0;

            OffsetDateTime addedAt = (box.getCreatedAt() != null)
                    ? box.getCreatedAt().atOffset(ZoneOffset.UTC)
                    : null;

            return ProductCompareResponse.ComparisonItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(product.getImageUrl())
                    .dietaryTags(Collections.emptyList())
                    .score(score)
                    .addedAt(addedAt)
                    .build();
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return ProductCompareResponse.builder()
                .savedCount(items.size())
                .products(items)
                .build();
    }
}