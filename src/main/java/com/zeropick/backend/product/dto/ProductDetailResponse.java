package com.zeropick.backend.product.dto;

import java.util.List;
import com.zeropick.backend.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductDetailResponse(
        @JsonProperty("productId") Long productId,
        @JsonProperty("productName") String productName,
        Integer grade,
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
                product.getGrade() != null ? product.getGrade().intValue() : 0,
                product.getWarningAdditive() != null ? product.getWarningAdditive() : false,
                new Nutrition(
                        product.getCalories() != null ? product.getCalories() : 0,
                        product.getSugar() != null ? product.getSugar().intValue() : 0,
                        product.getSodium() != null ? product.getSodium().intValue() : 0
                ),
                // ✅ 핵심: ingredientsAnalysis에 빈 배열 대신 더미 데이터 삽입
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