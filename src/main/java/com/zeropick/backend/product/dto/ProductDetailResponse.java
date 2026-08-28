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
        String imageUrl,

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
        if (product == null) return null;

        // 1. 이미지 URL이 null일 경우 기본 이미지 세팅 (DB 데이터 없을 때 대응)
        String fallbackImage = (product.getImageUrl() != null && !product.getImageUrl().isBlank())
                ? product.getImageUrl()
                : "https://via.placeholder.com/300x300.png?text=No+Image";

        // 2. 성분 분석 데이터 세팅 (임시 연동용 데이터 구성)
        Map<String, List<IngredientDetail>> ingredients = Map.of(
                "SAFE", List.of(
                        new IngredientDetail("알룰로스", "SAFE", "대체 감미료로 당류 부담이 적습니다."),
                        new IngredientDetail("스테비아", "SAFE", "천연 감미료로 혈당에 영향을 주지 않습니다.")
                )
        );

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getScore() != null ? product.getScore().intValue() : 0,
                product.getViewCount() != null ? product.getViewCount() : 0,
                fallbackImage,
                (product.getSummary() != null && !product.getSummary().isBlank())
                        ? product.getSummary()
                        : "특별한 감미료 이슈 없이 무난하게 구성되어 있어, 믿고 선택할 수 있는 제로 상품입니다.",
                new Nutrition(
                        product.getCalories() != null ? product.getCalories() : 0,
                        product.getSugar() != null ? product.getSugar().doubleValue() : 0.0,
                        product.getProtein() != null ? product.getProtein().doubleValue() : 0.0,
                        product.getSodium() != null ? product.getSodium().doubleValue() : 0.0
                ),
                ingredients
        );
    }
}