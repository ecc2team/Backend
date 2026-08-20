package com.zeropick.backend.product.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.zeropick.backend.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductDetailResponse(
        Long productId,

        @Schema(description = "상품명", example = "단백질 바 프로")
        String name,

        @Schema(description = "상품 점수", example = "92")
        Integer score,

        @Schema(description = "조회수", example = "120")
        Integer viewCount,

        @Schema(description = "상품 이미지 URL")
        String image,

        @Schema(description = "한줄평", example = "스테비아, 알룰로스 등 프리미엄 성분 위주로 구성되어 있어, 믿고 선택할 수 있는 제품입니다.")
        String summary,

        Nutrition nutrition,
        Map<String, List<IngredientDetail>> ingredientsAnalysis
) {
    public record Nutrition(
            Integer calories,
            Double sugar,
            Double protein,
            Double sodium
    ) {}

    public record IngredientDetail(
            String name,
            String riskLevel,
            String summary
    ) {}

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getScore() != null ? product.getScore().intValue() : 0,
                product.getViewCount() != null ? product.getViewCount() : 0,
                product.getImageUrl(),
                product.getSummary(),
                new Nutrition(
                        product.getCalories() != null ? product.getCalories() : 0,
                        product.getSugar() != null ? product.getSugar().doubleValue() : 0.0,
                        product.getProtein() != null ? product.getProtein().doubleValue() : 0.0,
                        product.getSodium() != null ? product.getSodium().doubleValue() : 0.0
                ),
                Collections.emptyMap() // 성분 분석 결과 Map (Service에서 조합하여 채워넣는 용도)
        );
    }
}