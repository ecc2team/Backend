package com.zeropick.backend.user.dto;

public record NutritionTargetResponse(
        int targetCalories,
        double targetCarbohydrate,
        double targetProtein,
        double targetSaturatedFat,
        double targetSugar,
        double targetSodium,
        boolean personalized
) {}