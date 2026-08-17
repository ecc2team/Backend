package com.zeropick.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest (
        @NotBlank String authCode
){
}
