package com.zeropick.backend.comparison.service;

import com.zeropick.backend.comparison.dto.ComparisonBoxToggleResponse;
import com.zeropick.backend.comparison.dto.ProductCompareResponse;
import com.zeropick.backend.comparison.entity.ComparisonBox;
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

    // 1. 비교함 상품 토글 (담기 / 빼기 - 카테고리 제한 제거)
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

        // 카테고리 일치 검사 로직 제거 (서로 다른 카테고리도 저장 가능)

        comparisonBoxRepository.save(new ComparisonBox(userId, productId));
        return new ComparisonBoxToggleResponse(productId, true);
    }

    // 2. 비교함 상품 삭제
    @Transactional
    public ComparisonBoxToggleResponse deleteComparisonBoxProduct(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        comparisonBoxRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(comparisonBoxRepository::delete);

        return new ComparisonBoxToggleResponse(productId, false);
    }

    // 3. 내 비교함 목록 조회 API (categoryCode 포함)
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

            // 카테고리 코드 변환 (String으로 안전 추출)
            String categoryCode = (product.getCategoryId() != null)
                    ? String.valueOf(product.getCategoryId())
                    : "ALL";

            // 접근 가능한 이미지 URL fallback 세팅
            String imageUrl = (product.getImageUrl() != null && !product.getImageUrl().isBlank())
                    ? product.getImageUrl()
                    : "https://picsum.photos/300/300"; // 브라우저 차단 없는 공개 샘플 이미지

            return ProductCompareResponse.ComparisonItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(imageUrl)
                    .categoryCode(categoryCode) // categoryCode 매핑
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