package com.zeropick.backend.user.dto;

import com.zeropick.backend.user.AuthProvider;

public record FindAccountResponse (
        String email,
        AuthProvider provider
) {
}
