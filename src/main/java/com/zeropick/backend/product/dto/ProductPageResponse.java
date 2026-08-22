package com.zeropick.backend.product.dto;

import com.zeropick.backend.product.entity.Product;
import lombok.Getter;

import java.util.List;

public record ProductPageResponse(List<ProductItem> content, PageInfo pageInfo) {

    public record ProductItem(Long productId, String name, Integer score, Double calories, Double sugar,
                              Boolean warningAdditive, Integer viewCount, Integer compareCount) {

        public static ProductItem from(Product product) {
                return new ProductItem(
                        product.getId(),
                        product.getName(),
                        product.getScore() != null ? product.getScore().intValue() : null,
                        // valueOf 대신 IDE가 추천한 parseDouble / parseInt 사용 (경고 제거)
                        product.getCalories() != null ? Double.parseDouble(product.getCalories().toString()) : 0.0,
                        product.getSugar() != null ? product.getSugar().doubleValue() : 0.0,
                        product.getWarningAdditive() != null ? product.getWarningAdditive() : false,
                        product.getViewCount() != null ? Integer.parseInt(product.getViewCount().toString()) : 0,
                        product.getCompareCount() != null ? Integer.parseInt(product.getCompareCount().toString()) : 0
                );
            }
        }

    @Getter
    public static class PageInfo {
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean isLast;

        public PageInfo(int page, int size, long totalElements, int totalPages, boolean isLast) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.isLast = isLast;
        }
    }
}