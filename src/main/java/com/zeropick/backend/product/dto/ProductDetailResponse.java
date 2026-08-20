package com.zeropick.backend.product.dto;

import java.util.List;
import com.zeropick.backend.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductDetailResponse(
        @JsonProperty("productId") Long productId,
        @JsonProperty("productName") String productName,

        @Schema(description = "상품 점수", example = "85")
        Integer score,                          // 👈 1. grade 대신 score로 변경

        @Schema(description = "조회수", example = "120")
        Long viewCount,                         // 👈 2. 조회수 추가

        @Schema(description = "한줄평", example = "특별한 감미료 이슈 없이 무난하게 구성된 제품입니다.")
        String summary,                         // 👈 3. 한줄평 추가

        @Schema(description = "상품 이미지 URL")
        String image,                           // 👈 4. 이미지 URL 추가

        Boolean warningAdditive,
        Nutrition nutrition,
        IngredientsAnalysis ingredientsAnalysis
) {
    public record Nutrition(
            Integer calories,
            Integer sugar,
            Integer sodium
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
                product.getScore() != null ? product.getScore().intValue() : 0,           // score 매핑
                product.getViewCount() != null ? product.getViewCount() : 0L,  // viewCount 매핑
                product.getSummary(),                                           // summary 매핑
                product.getImageUrl(),                                          // image 매핑 (엔티티 필드명에 맞게 getImage()로 수정 가능)
                product.getWarningAdditive() != null ? product.getWarningAdditive() : false,
                new Nutrition(
                        product.getCalories() != null ? product.getCalories() : 0,
                        product.getSugar() != null ? product.getSugar().intValue() : 0,
                        product.getSodium() != null ? product.getSodium().intValue() : 0
                ),
                new IngredientsAnalysis(
                        List.of(
                                new SweetenerDetail("수크랄로스", "GENERAL", "일반적인 2등급 대체당")
                        ),
                        List.of(
                                new AdditiveDetail("카라멜색소", "WARNING", "노화 촉진 성분 함유")
                        )
                )
        );
    }
}