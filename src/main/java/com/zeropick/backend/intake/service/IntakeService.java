package com.zeropick.backend.intake.service;

import com.zeropick.backend.intake.dto.TodayIntakeSummaryResponse;
import com.zeropick.backend.intake.entity.IntakeRecord;
import com.zeropick.backend.intake.repository.IntakeRecordRepository;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.repository.ProductRepository;
import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.repository.UserRepository;
import com.zeropick.backend.user.util.NutritionCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntakeService {

    private final IntakeRecordRepository intakeRecordRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NutritionCalculator nutritionCalculator;

    // 1. 오늘 먹은 제품 기록 추가 (20번 API)
    @Transactional
    public void addIntakeRecord(Long userId, Long productId, BigDecimal quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("기록할 상품 ID(productId)가 전달되지 않았습니다.");
        }

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId);
        }

        BigDecimal actualQuantity = (quantity != null) ? quantity : BigDecimal.valueOf(1.0);
        IntakeRecord record = new IntakeRecord(userId, productId, OffsetDateTime.now(), actualQuantity);
        intakeRecordRepository.save(record);
    }

    // 2. 오늘의 섭취량 및 목록 조회 (21번 API)
    public TodayIntakeSummaryResponse getTodayIntakeSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        NutritionCalculator.NutrientTarget target = nutritionCalculator.calculate(user);

        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zoneId);

        OffsetDateTime startOfDay = today.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = today.atTime(LocalTime.MAX).atZone(zoneId).toOffsetDateTime();

        List<IntakeRecord> todayRecords = intakeRecordRepository.findByUserIdAndIntakeAtBetween(userId, startOfDay, endOfDay);

        int totalCalories = 0;
        double totalSugar = 0.0;
        double totalSodium = 0.0;
        double totalProtein = 0.0;
        double totalSaturatedFat = 0.0;
        double totalCarbohydrate = 0.0;

        List<TodayIntakeSummaryResponse.IntakeDetail> intakeDetails = new ArrayList<>();

        for (IntakeRecord record : todayRecords) {
            Product product = productRepository.findById(record.getProductId()).orElse(null);
            if (product != null) {
                double qty = record.getQuantity() != null ? record.getQuantity().doubleValue() : 1.0;

                int itemCalories = (int) Math.round((product.getCalories() != null ? product.getCalories() : 0) * qty);
                totalCalories += itemCalories;

                totalSugar += (product.getSugar() != null ? product.getSugar().doubleValue() : 0.0) * qty;
                totalSodium += (product.getSodium() != null ? product.getSodium().doubleValue() : 0.0) * qty;
                totalProtein += (product.getProtein() != null ? product.getProtein().doubleValue() : 0.0) * qty;
                totalSaturatedFat += (product.getSaturatedFat() != null ? product.getSaturatedFat().doubleValue() : 0.0) * qty;
                totalCarbohydrate += (product.getCarbohydrate() != null ? product.getCarbohydrate().doubleValue() : 0.0) * qty;

                TodayIntakeSummaryResponse.IntakeDetail detail = TodayIntakeSummaryResponse.IntakeDetail.builder()
                        .intakeRecordId(record.getId())
                        .intakeTime(record.getIntakeAt() != null ? record.getIntakeAt().format(DateTimeFormatter.ofPattern("HH:mm")) : null)
                        .productName(product.getName())
                        .servingSize("1개")
                        .calories(itemCalories)
                        .build();

                intakeDetails.add(detail);
            }
        }

        int targetCals = target.targetCalories() > 0 ? target.targetCalories() : 2000;
        int calorieGaugePercentage = (int) Math.round(((double) totalCalories / targetCals) * 100);
        String statusMessage = calorieGaugePercentage <= 100 ? "아직 여유가 있어요!" : "권장 칼로리를 초과했어요!";

        double tSugar = target.targetSugar() > 0 ? target.targetSugar() : 1;
        double tSodium = target.targetSodium() > 0 ? target.targetSodium() : 1;
        double tSatFat = target.targetSaturatedFat() > 0 ? target.targetSaturatedFat() : 1;
        double tProtein = target.targetProtein() > 0 ? target.targetProtein() : 1;
        double tCarbo = target.targetCarbohydrate() > 0 ? target.targetCarbohydrate() : 1;

        TodayIntakeSummaryResponse.Summary summary = TodayIntakeSummaryResponse.Summary.builder()
                .totalCalories(totalCalories)
                .targetCalories(targetCals)
                .calorieGaugePercentage(calorieGaugePercentage)
                .statusMessage(statusMessage)
                .build();

        TodayIntakeSummaryResponse.Nutrients nutrients = TodayIntakeSummaryResponse.Nutrients.builder()
                .sugarPercentage((int) Math.round((totalSugar / tSugar) * 100))
                .sodiumPercentage((int) Math.round((totalSodium / tSodium) * 100))
                .saturatedFatPercentage((int) Math.round((totalSaturatedFat / tSatFat) * 100))
                .proteinPercentage((int) Math.round((totalProtein / tProtein) * 100))
                .carbohydratePercentage((int) Math.round((totalCarbohydrate / tCarbo) * 100))
                .build();

        return TodayIntakeSummaryResponse.builder()
                .date(today.toString())
                .summary(summary)
                .nutrients(nutrients)
                .intakeDetails(intakeDetails)
                .build();
    }

    // 3. 섭취 기록 삭제 (28번 API)
    @Transactional
    public void deleteIntakeRecord(String intakeRecordId) {
        if (intakeRecordId == null || intakeRecordId.isBlank()) {
            return;
        }

        try {
            Long id = Long.parseLong(intakeRecordId);
            if (intakeRecordRepository.existsById(id)) {
                intakeRecordRepository.deleteById(id);
            }
        } catch (NumberFormatException e) {
            // UUID 문자열 들어올 경우 500 에러 방지
        }
    }
}