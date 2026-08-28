package com.zeropick.backend.auth.dto;

public record SocialLoginResult(Long userId, boolean isNewUser, String accessToken, String refreshToken) {
    public SocialLoginResponse toResponse() {
        return new SocialLoginResponse(userId, isNewUser, accessToken);
    }
}