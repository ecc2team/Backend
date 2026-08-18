package com.zeropick.backend.user.dto;

import com.zeropick.backend.user.AuthProvider;

public record FindAccountResponse (
        Long userId,
        String email,
        AuthProvider provider
) {
}
