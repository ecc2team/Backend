package com.zeropick.backend.intake.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IntakeRecordRequest {
    private Long productId; // 기록할 상품 ID
    private Integer amount;  // 섭취량/수량 (필요 시 필드 추가)
}