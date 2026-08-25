package com.zeropick.backend.intake.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class IntakeRecordRequest {
    private Long productId;     // 기록할 상품 ID
    private BigDecimal quantity; // 섭취 수량
}