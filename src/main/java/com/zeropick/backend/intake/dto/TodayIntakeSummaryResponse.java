package com.zeropick.backend.intake.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record TodayIntakeSummaryResponse(
        String date,
        Summary summary,
        Nutrients nutrients,
        List<IntakeDetail> intakeDetails
) {
    @Builder
    public record Summary(
            int totalCalories,
            int targetCalories,
            int calorieGaugePercentage,
            String statusMessage
    ) {}

    @Builder
    public record Nutrients(
            int sugarPercentage,
            int sodiumPercentage,
            int saturatedFatPercentage,
            int proteinPercentage,
            int carbohydratePercentage
    ) {}

    @Builder
    public record IntakeDetail(
            Long intakeRecordId,
            String intakeTime,
            String productName,
            String servingSize,
            int calories
    ) {}
}