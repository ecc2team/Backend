package com.zeropick.backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RecentProductsResponse {

    private Integer totalElements;
    private List<RecentProductItem> content;

    @Getter
    @AllArgsConstructor
    public static class RecentProductItem {
        private Long productId;
        private String productName;
        private List<String> dietaryTags;
        private String riskLevel;
        private String viewedAt;
    }
}