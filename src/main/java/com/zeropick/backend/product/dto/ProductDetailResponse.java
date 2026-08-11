package com.zeropick.backend.product.dto;

import java.util.List;

public record ProductDetailResponse(
        Long id,
        String name,
        Integer grade,
        Boolean warningAdditive,
        Nutrition nutrition,
        IngredientsAnalysis analysis
) {
    // 중첩 record 생성
    public record Nutrition(
            Integer calories,
            Integer sugar,
            Integer sodium
    ) {}

    public record IngredientsAnalysis(
            List<String> cautionIngredients,
            List<String> allergicIngredients
    ) {}
}