package com.zeropick.backend.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCompareResponse {

    private Integer comparedCount;
    private List<ComparisonItem> comparisonTable;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComparisonItem {
        private Long productId;
        private String productName;
        private String imageUrl;
        private Integer score;
        private Boolean warningAdditive;
        private List<String> topBadges;
        private Nutrition nutrition;
        private List<String> keyIngredients;
        private List<String> allergies;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Nutrition {
        private Integer calories;
        private Double sugar;
        private Double sodium;
    }
}