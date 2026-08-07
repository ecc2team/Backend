package com.zeropick.backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductDetailResponse {

    private Long productId;
    private String productName;
    private Integer grade;
    private Boolean warningAdditive;
    private Nutrition nutrition;
    private IngredientsAnalysis ingredientsAnalysis;

    @Getter
    @AllArgsConstructor
    public static class Nutrition {
        private Integer calories;
        private Integer sugar;
        private Integer sodium;
    }

    @Getter
    @AllArgsConstructor
    public static class IngredientsAnalysis {
        private List<IngredientInfo> sweeteners;
        private List<IngredientInfo> additives;
    }

    @Getter
    @AllArgsConstructor
    public static class IngredientInfo {
        private String name;
        private String riskLevel;
        private String summary;
    }
}