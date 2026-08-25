package com.zeropick.backend.intake.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntakeRecordRequest {
    private Long productId;     // 기록할 상품 ID
    private BigDecimal quantity; // 섭취 수량
}