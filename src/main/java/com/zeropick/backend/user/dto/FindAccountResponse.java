package com.zeropick.backend.user.dto;

import com.zeropick.backend.user.enums.AuthProvider;

public record FindAccountResponse (
        Long userId,
        String email,
        AuthProvider provider
) {
}
