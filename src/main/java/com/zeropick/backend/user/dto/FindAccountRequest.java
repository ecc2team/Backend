package com.zeropick.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FindAccountRequest(
        @NotBlank @Email String email
) {
}
