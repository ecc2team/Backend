package com.zeropick.backend.intake.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyIntakeResponse {
    private String date;
    private Double safeLimitMax;
    private Double currentIntake;
    private Double gaugePercentage;
    private String warningMessage;
}