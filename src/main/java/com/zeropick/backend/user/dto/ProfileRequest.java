package com.zeropick.backend.user.dto;

import com.zeropick.backend.user.enums.ActivityLevel;
import com.zeropick.backend.user.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProfileRequest(
        Gender gender,
        LocalDate birthDate,
        BigDecimal height,
        BigDecimal weight,
        ActivityLevel activityLevel
) {}