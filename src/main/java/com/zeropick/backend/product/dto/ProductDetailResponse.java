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
    // Product 엔티티를 ProductDetailResponse로 변환하는 정적 메서드
    // Product 엔티티를 ProductDetailResponse로 변환하는 정적 메서드
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),               // 1. id
                product.getName(),             // 2. name
                product.getGrade() != null ? product.getGrade().intValue() : 0,// 3. grade (실제 DB 등급 값)
                product.getWarningAdditive(),  // 4. warningAdditive (실제 주의 성분 유무)
                new Nutrition(
                        product.getCalories() != null ? product.getCalories() : 0,
                        product.getSugar() != null ? product.getSugar().intValue() : 0,   // 👈 .intValue() 추가!
                        product.getSodium() != null ? product.getSodium().intValue() : 0  // 👈 .intValue() 추가!
                ),
                new IngredientsAnalysis(List.of(), List.of()) // 6. analysis (엔티티에 필드가 없으므로 기본값 유지)
        );
    }
}