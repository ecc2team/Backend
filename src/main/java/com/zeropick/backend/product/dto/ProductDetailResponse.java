package com.zeropick.backend.product.dto;

import java.util.Collections;
import java.util.List;
import com.zeropick.backend.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductDetailResponse(
        Long productId,
        String productName,

        @Schema(description = "상품 점수", example = "85")
        Integer score,

        @Schema(description = "조회수", example = "120")
        Integer viewCount,

        @Schema(description = "한줄평", example = "특별한 감미료 이슈 없이 무난하게 구성된 제품입니다.")
        String summary,

        @Schema(description = "상품 이미지 URL")
        String image,

        Boolean warningAdditive,
        Nutrition nutrition,
        IngredientsAnalysis ingredientsAnalysis
) {
    public record Nutrition(
            Integer calories,
            Double sugar,
            Double sodium
    ) {}

    public record IngredientsAnalysis(
            List<SweetenerDetail> sweeteners,
            List<AdditiveDetail> additives
    ) {}

    public record SweetenerDetail(
            String name,
            String riskLevel,
            String summary
    ) {}

    public record AdditiveDetail(
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
                product.getSummary(),
                product.getImageUrl(),
                product.getWarningAdditive() != null ? product.getWarningAdditive() : false,
                new Nutrition(
                        product.getCalories() != null ? product.getCalories() : 0,
                        product.getSugar() != null ? product.getSugar().doubleValue() : 0.0,
                        product.getSodium() != null ? product.getSodium().doubleValue() : 0.0
                ),
                new IngredientsAnalysis(Collections.emptyList(), Collections.emptyList())
        );
    }
}