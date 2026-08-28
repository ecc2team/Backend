package com.zeropick.backend.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCompareResponse {

    private Integer savedCount;
    private List<ComparisonItem> products;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonItem {
        private Long productId;
        private String productName;
        private String imageUrl;
        private String categoryCode; // <- 추가된 카테고리 코드 필드
        private List<String> dietaryTags;
        private Integer score;
        private OffsetDateTime addedAt;
    }
}