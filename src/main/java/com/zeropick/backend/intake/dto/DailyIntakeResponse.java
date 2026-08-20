package com.zeropick.backend.intake.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List; // 1. List import 추가

@Getter
@AllArgsConstructor
public class DailyIntakeResponse {
    private String date;
    private Double safeLimitMax;
    private Double currentIntake;
    private Double gaugePercentage;
    private String warningMessage;

    // 2. 오늘 섭취 목록 필드 추가
    private List<IntakeItemDto> intakeList;
}