package com.zeropick.backend.product.dto;

import java.util.List;
import com.zeropick.backend.product.entity.Product;

public record ProductDetailResponse(
        Long id,
        String name,
        Integer grade,
        Boolean warningAdditive,
        Nutrition nutrition,
        IngredientsAnalysis analysis
) {
    // 중첩 record 1: 영양 성분 정보
    public record Nutrition(
            Integer calories,
            Integer sugar,
            Integer sodium
    ) {}

    // 중첩 record 2: 성분 분석 정보
    public record IngredientsAnalysis(
            List<String> cautionIngredients,
            List<String> allergicIngredients
    ) {}

    // Product 엔티티를 ProductDetailResponse로 변환하는 정적 메서드
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),                             // 1. id
                product.getName(),                           // 2. name
                0,                                           // 3. grade (임시 기본값)
                false,                                       // 4. warningAdditive (임시 기본값)
                new Nutrition(0, 0, 0),                      // 5. nutrition (임시 기본 객체)
                new IngredientsAnalysis(List.of(), List.of()) // 6. analysis (임시 기본 객체)
        );
    }
}