package com.zeropick.backend.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ComparisonBoxListResponse {

    private Integer savedCount;
    private List<ComparisonProductItem> products;

    @Getter
    @AllArgsConstructor
    public static class ComparisonProductItem {
        private Long productId;
        private String productName;
        private String imageUrl;
        private List<String> dietaryTags;
        private String riskLevel;
        private String addedAt;
    }
}