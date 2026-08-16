package com.zeropick.backend.product.dto;

import java.util.List;
import com.zeropick.backend.product.entity.Product;

public record ProductDetailResponse(
        Long id,
        String name,
        Integer grade,
        Boolean warningAdditive,
        Nutrition nutrition,
        IngredientsAnalysis ingredientsAnalysis // 👈 JSON 필드명("ingredientsAnalysis")에 맞추기 위해 변수명 변경
) {
    // 중첩 record 1: 영양 성분 정보
    public record Nutrition(
            Integer calories,
            Integer sugar,
            Integer sodium
    ) {}

    // 중첩 record 2: 성분 분석 정보 (대체당, 첨가물 리스트 포함)
    public record IngredientsAnalysis(
            List<SweetenerDetail> sweeteners,
            List<AdditiveDetail> additives
    ) {}

    // 중첩 record 3: 대체당 상세 정보
    public record SweetenerDetail(
            String name,
            String riskLevel,
            String summary
    ) {}

    // 중첩 record 4: 첨가물 상세 정보
    public record AdditiveDetail(
            String name,
            String riskLevel,
            String summary
    ) {}

    // Product 엔티티를 ProductDetailResponse로 변환하는 정적 메서드
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),                                                // 1. id
                product.getName(),                                              // 2. name
                product.getGrade() != null ? product.getGrade().intValue() : 0, // 3. grade
                product.getWarningAdditive(),                                   // 4. warningAdditive
                new Nutrition(                                                  // 5. nutrition
                        product.getCalories() != null ? product.getCalories() : 0,
                        product.getSugar() != null ? product.getSugar().intValue() : 0,
                        product.getSodium() != null ? product.getSodium().intValue() : 0
                ),
                // 6. ingredientsAnalysis (프론트 요청 명세서에 맞춘 더미 데이터 매핑)
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