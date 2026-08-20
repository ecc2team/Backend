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
        // 유저 검증
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        Optional<ComparisonBox> existingBox = comparisonBoxRepository.findByUserIdAndProductId(userId, productId);

        // 이미 담겨있다면 삭제 (isInComparisonBox: false)
        if (existingBox.isPresent()) {
            comparisonBoxRepository.delete(existingBox.get());
            return new ComparisonBoxToggleResponse(productId, false);
        }

        // 담겨있지 않다면 카테고리 검증 후 새로 추가 (isInComparisonBox: true)
        Product newProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        List<ComparisonBox> userBoxes = comparisonBoxRepository.findByUserId(userId);
        if (!userBoxes.isEmpty()) {
            Long existingProductId = userBoxes.get(0).getProductId();
            Product existingProduct = productRepository.findById(existingProductId).orElse(null);

            // 다른 카테고리의 상품이 이미 들어있는 경우 (IllegalArgumentException 처리로 400 반환)
            if (existingProduct != null && !existingProduct.getCategoryId().equals(newProduct.getCategoryId())) {
                throw new IllegalArgumentException("같은 카테고리의 상품만 비교할 수 있습니다.");
            }
        }

        comparisonBoxRepository.save(new ComparisonBox(userId, productId));
        return new ComparisonBoxToggleResponse(productId, true);
    }

    // 2. 비교함 상품 삭제
    @Transactional
    public ComparisonBoxToggleResponse deleteComparisonBoxProduct(Long userId, Long productId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        // 비교함에 존재하지 않는 경우 IllegalArgumentException 던짐 (400 반환)
        ComparisonBox box = comparisonBoxRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("비교함에 해당 상품이 존재하지 않습니다."));

        comparisonBoxRepository.delete(box);

        return new ComparisonBoxToggleResponse(productId, false);
    }

    // 3. 비교함 전체 목록 조회
    public ProductCompareResponse getComparisonTable(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        List<ComparisonBox> boxes = comparisonBoxRepository.findByUserId(userId);

        List<ProductCompareResponse.ComparisonItem> items = boxes.stream().map(box -> {
            Product product = productRepository.findById(box.getProductId()).orElse(null);
            if (product == null) return null;

            ProductCompareResponse.Nutrition nutrition = ProductCompareResponse.Nutrition.builder()
                    .calories(product.getCalories())
                    .sugar(product.getSugar() != null ? product.getSugar().doubleValue() : 0.0)
                    .sodium(product.getSodium() != null ? product.getSodium().doubleValue() : 0.0)
                    .build();

            return ProductCompareResponse.ComparisonItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .imageUrl(product.getImageUrl())
                    .score(product.getScore() != null ? product.getScore().intValue() : 0)
                    .warningAdditive(product.getWarningAdditive())
                    .topBadges(List.of()) // 추후 필요 시 데이터 연동
                    .nutrition(nutrition)
                    .keyIngredients(List.of()) // 추후 필요 시 데이터 연동
                    .allergies(List.of()) // 추후 필요 시 데이터 연동
                    .build();
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return new ProductCompareResponse(items.size(), items);
    }
}