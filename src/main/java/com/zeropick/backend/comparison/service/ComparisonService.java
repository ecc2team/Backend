package com.zeropick.backend.comparison.service;

import com.zeropick.backend.comparison.dto.ProductCompareResponse;
import com.zeropick.backend.comparison.entity.ComparisonBox;
import com.zeropick.backend.comparison.repository.ComparisonBoxRepository;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComparisonService {

    private final ComparisonBoxRepository comparisonBoxRepository;
    private final ProductRepository productRepository;

    // 1. 비교함 상품 추가
    @Transactional
    public void addComparisonBox(Long userId, Long productId) {
        if (!comparisonBoxRepository.existsByUserIdAndProductId(userId, productId)) {
            comparisonBoxRepository.save(new ComparisonBox(userId, productId));
        }
    }

    // 2. 비교함 목록 및 제품 비교 조회
    public ProductCompareResponse getComparisonTable(Long userId) {
        List<ComparisonBox> boxes = comparisonBoxRepository.findByUserId(userId);

        List<ProductCompareResponse.ComparisonItem> items = boxes.stream().map(box -> {
            Product product = productRepository.findById(box.getProductId()).orElse(null);
            if (product == null) return null;

            ProductCompareResponse.Nutrition nutrition = new ProductCompareResponse.Nutrition(
                    product.getCalories(),
                    product.getSugar() != null ? product.getSugar().doubleValue() : 0.0,
                    product.getSodium() != null ? product.getSodium().doubleValue() : 0.0
            );

            ProductCompareResponse.KeyIngredients keyIngredients = new ProductCompareResponse.KeyIngredients(
                    List.of("감미료"), List.of("첨가물")
            );

            return new ProductCompareResponse.ComparisonItem(
                    product.getId(),
                    product.getName(),
                    product.getGrade(),
                    product.getWarningAdditive(),
                    nutrition,
                    keyIngredients,
                    List.of()
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return new ProductCompareResponse(items.size(), items);
    }

    // 3. 비교함 상품 삭제
    @Transactional
    public void deleteComparisonBox(Long userId, Long productId) {
        comparisonBoxRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(comparisonBoxRepository::delete);
    }
}