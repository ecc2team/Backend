package com.zeropick.backend.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductCompareResponse {

    private Integer comparedCount;
    private List<ComparisonItem> comparisonTable;

    @Getter
    @AllArgsConstructor
    public static class ComparisonItem {
        private Long productId;
        private String productName;
        private Integer score; // 👈 Short grade -> Integer score로 변경
        private Boolean warningAdditive;
        private Nutrition nutrition;
        private KeyIngredients keyIngredients;
        private List<String> allergies;
    }

    @Getter
    @AllArgsConstructor
    public static class Nutrition {
        private Integer calories;
        private Double sugar;
        private Double sodium;
    }

    @Getter
    @AllArgsConstructor
    public static class KeyIngredients {
        private List<String> sweeteners;
        private List<String> additives;
    }
}