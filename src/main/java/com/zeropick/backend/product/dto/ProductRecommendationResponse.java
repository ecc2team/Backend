package com.zeropick.backend.product.dto;

import com.zeropick.backend.product.entity.Product;

import java.util.List;

public record ProductRecommendationResponse(
        List<RecommendedProduct> content
) {
    public record RecommendedProduct(
            Long productId,
            String name,
            Integer score,
            String imageUrl,
            Integer calories,
            Double sugar,
            Boolean warningAdditive,
            Integer viewCount,
            // true: 선호 카테고리 안에서 뽑힌 추천, false: 카테고리 무관 폴백으로 채워진 추천
            boolean matchedPreference
    ){
        public static RecommendedProduct from(Product product, boolean matchedPreference) {
            return new RecommendedProduct(
                    product.getId(),
                    product.getName(),
                    product.getScore() != null ? product.getScore().intValue() : null,
                    product.getImageUrl(),
                    product.getCalories(),
                    product.getSugar() != null ? product.getSugar().doubleValue() : 0.0,
                    product.getWarningAdditive() != null ? product.getWarningAdditive() : false,
                    product.getViewCount(),
                    matchedPreference

            );
        }
    }
}
