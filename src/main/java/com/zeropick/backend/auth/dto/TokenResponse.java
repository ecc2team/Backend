package com.zeropick.backend.auth.dto;

public record TokenResponse(Long userId, String accessToken, String refreshToken) {
}
