package com.zeropick.backend.user.dto;

public record EmailCheckResponse(
        String email,
        boolean isAvailable
) {}
