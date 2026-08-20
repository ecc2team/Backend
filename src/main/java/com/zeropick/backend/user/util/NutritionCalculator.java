package com.zeropick.backend.user.util;

import com.zeropick.backend.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class NutritionCalculator {

    // 목표치를 담을 내부 DTO 레코드 (Java 16+)
    public record NutrientTarget(
            int targetCalories,
            double targetCarbohydrate,
            double targetProtein,
            double targetSaturatedFat,
            double targetSugar,
            double targetSodium
    ) {}

    public NutrientTarget calculate(User user) {
        // 신체 정보가 하나라도 없으면 한국인 성인 평균(2000kcal) 기준으로 기본값 반환
        if (user.getWeight() == null || user.getHeight() == null || user.getBirthDate() == null) {
            return new NutrientTarget(2000, 250.0, 50.0, 15.0, 50.0, 2000.0);
        }

        // 1. 나이 계산
        int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        double weight = user.getWeight().doubleValue();
        double height = user.getHeight().doubleValue();

        // 2. 기초 대사량(BMR) 계산
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        if ("MALE".equalsIgnoreCase(user.getGender())) {
            bmr += 5;
        } else {
            bmr -= 161;
        }

        // 3. 활동 계수 적용
        double activityMultiplier = getActivityMultiplier(user.getActivityLevel());
        int targetCalories = (int) Math.round(bmr * activityMultiplier);

        // 4. 영양소 목표치 계산
        double targetCarbohydrate = (targetCalories * 0.5) / 4.0;
        double targetProtein = (targetCalories * 0.2) / 4.0;
        double targetSaturatedFat = (targetCalories * 0.1) / 9.0;
        double targetSugar = (targetCalories * 0.1) / 4.0;
        double targetSodium = 2000.0; // 나트륨은 2000mg 고정

        return new NutrientTarget(
                targetCalories,
                targetCarbohydrate,
                targetProtein,
                targetSaturatedFat,
                targetSugar,
                targetSodium
        );
    }

    private double getActivityMultiplier(String activityLevel) {
        if (activityLevel == null) return 1.2; // 기본값
        return switch (activityLevel.toUpperCase()) {
            case "LIGHT" -> 1.375;
            case "MODERATE" -> 1.55;
            case "ACTIVE" -> 1.725;
            case "VERY_ACTIVE" -> 1.9;
            default -> 1.2; // "SEDENTARY" 등
        };
    }
}