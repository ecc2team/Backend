package com.zeropick.backend.intake.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class IntakeCreateRequest {
    private Long productId;
    private Integer quantity;
}