package com.zeropick.backend.intake.dto;

public record IntakeCreateRequest(
        Long productId,
        Integer quantity
) {}