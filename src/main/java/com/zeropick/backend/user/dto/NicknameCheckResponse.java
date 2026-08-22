package com.zeropick.backend.user.dto;

public record NicknameCheckResponse(
        String nickname,
        boolean isAvailable
) {}