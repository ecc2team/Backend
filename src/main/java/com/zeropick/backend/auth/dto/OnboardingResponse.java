package com.zeropick.backend.auth.dto;

public record OnboardingResponse(
        Long userId,
        String nickname
) {}