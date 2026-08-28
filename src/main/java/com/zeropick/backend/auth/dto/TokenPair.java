package com.zeropick.backend.auth.dto;

public record TokenPair(Long userId, String accessToken, String refreshToken) {
    public TokenResponse toResponse() { return new TokenResponse(userId, accessToken); }
}