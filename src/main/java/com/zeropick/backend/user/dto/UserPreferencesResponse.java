package com.zeropick.backend.user.dto;

import java.time.OffsetDateTime;

public record UserPreferencesResponse(
        Long userId,
        OffsetDateTime updatedAt
) {
}