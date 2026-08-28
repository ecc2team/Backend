package com.zeropick.backend.user.util;

import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.enums.ActivityLevel;
import com.zeropick.backend.user.enums.Gender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class NutritionCalculator {

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

        // 분리한 메서드를 호출하여 목표 칼로리 계산
        int targetCalories = calculateTargetCalories(user);

        // 영양소 목표치 계산
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

    // 목표 칼로리 계산 전용 메서드
    private int calculateTargetCalories(User user) {
        int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        double weight = user.getWeight().doubleValue();
        double height = user.getHeight().doubleValue();

        // 기초 대사량(BMR) 계산
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);

        // Gender Enum을 활용한 성별 보정 로직 (NONE 처리 포함)
        if (user.getGender() == Gender.MALE) {
            bmr += 5;
        } else if (user.getGender() == Gender.FEMALE) {
            bmr -= 161;
        } else {
            // 성별 선택 안 함 또는 null 인 경우: 남녀 보정치의 평균값(-78) 적용
            bmr -= 78;
        }

        double activityMultiplier = getActivityMultiplier(user.getActivityLevel());
        return (int) Math.round(bmr * activityMultiplier);
    }

    // ActivityLevel Enum을 활용한 활동 계수 반환
    private double getActivityMultiplier(ActivityLevel activityLevel) {
        if (activityLevel == null) return 1.2; // 기본값

        return switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTRA_ACTIVE -> 1.9;
        };
    }
}