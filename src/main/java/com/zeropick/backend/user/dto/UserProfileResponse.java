package com.zeropick.backend.user.dto;

import com.zeropick.backend.user.enums.AuthProvider;
import com.zeropick.backend.user.enums.ActivityLevel;
import com.zeropick.backend.user.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        AuthProvider provider,
        Gender gender,
        LocalDate birthDate,
        BigDecimal height,
        BigDecimal weight,
        ActivityLevel activityLevel,
        List<String> preferredCategories,
        List<String> dislikedIngredients,
        List<String> allergyFlags
) {}