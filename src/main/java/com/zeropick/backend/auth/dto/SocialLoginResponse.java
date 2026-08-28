package com.zeropick.backend.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SocialLoginResponse(
        Long userId,
        @JsonProperty("isNewUser") boolean isNewUser,
        String accessToken
) {
}