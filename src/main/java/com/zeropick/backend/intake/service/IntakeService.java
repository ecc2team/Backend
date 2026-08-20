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

    // 1. 섭취 기록 추가
    @Transactional
    public void addIntakeRecord(Long userId, Long productId, BigDecimal quantity) {
        // 제품 존재 여부 검증 로직 추가
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("존재하지 않는 제품입니다.");
        }

        BigDecimal actualQuantity = (quantity != null) ? quantity : BigDecimal.valueOf(1.0);
        IntakeRecord record = new IntakeRecord(userId, productId, OffsetDateTime.now(), actualQuantity);
        intakeRecordRepository.save(record);
    }

    // 2. 오늘의 섭취량 조회 및 계산
    public TodayIntakeSummaryResponse getTodayIntakeSummary(Long userId) {

        // 1) 유저 정보 조회 및 개인화된 목표 칼로리/영양소 계산
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        NutritionCalculator.NutrientTarget target = nutritionCalculator.calculate(user);

        // 2) 오늘 날짜/시간 기준 셋팅
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zoneId);

        OffsetDateTime startOfDay = today.atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = today.atTime(LocalTime.MAX).atZone(zoneId).toOffsetDateTime();

        // 3) 오늘의 섭취 기록 조회
        List<IntakeRecord> todayRecords = intakeRecordRepository.findByUserIdAndIntakeAtBetween(userId, startOfDay, endOfDay);

        // 4) 합계 계산용 변수 초기화
        int totalCalories = 0;
        double totalSugar = 0.0;
        double totalSodium = 0.0;
        double totalProtein = 0.0;
        double totalSaturatedFat = 0.0;
        double totalCarbohydrate = 0.0; // 식이섬유 대신 탄수화물

        List<TodayIntakeSummaryResponse.IntakeDetail> intakeDetails = new ArrayList<>();

        // 5) 섭취 기록 순회하며 영양소 합산 및 상세 리스트(DTO) 생성
        for (IntakeRecord record : todayRecords) {
            Product product = productRepository.findById(record.getProductId()).orElse(null);
            if (product != null) {
                double qty = record.getQuantity().doubleValue();

                int itemCalories = (int) Math.round((product.getCalories() != null ? product.getCalories() : 0) * qty);
                totalCalories += itemCalories;

                totalSugar += (product.getSugar() != null ? product.getSugar().doubleValue() : 0.0) * qty;
                totalSodium += (product.getSodium() != null ? product.getSodium().doubleValue() : 0.0) * qty;
                totalProtein += (product.getProtein() != null ? product.getProtein().doubleValue() : 0.0) * qty;
                totalSaturatedFat += (product.getSaturatedFat() != null ? product.getSaturatedFat().doubleValue() : 0.0) * qty;
                totalCarbohydrate += (product.getCarbohydrate() != null ? product.getCarbohydrate().doubleValue() : 0.0) * qty;

                // 개별 섭취 기록 DTO(Record) 조립
                TodayIntakeSummaryResponse.IntakeDetail detail = TodayIntakeSummaryResponse.IntakeDetail.builder()
                        .intakeRecordId(record.getId())
                        .intakeTime(record.getIntakeAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                        .productName(product.getName())
                        .servingSize("1개") // TODO: 제공 단위가 있다면 수정
                        .calories(itemCalories)
                        .build();

                intakeDetails.add(detail);
            }
        }

        // 6) 달성률 및 상태 메시지 계산 (Target 기준 방어 로직 적용)
        int targetCals = target.targetCalories() > 0 ? target.targetCalories() : 2000;
        int calorieGaugePercentage = (int) Math.round(((double) totalCalories / targetCals) * 100);
        String statusMessage = calorieGaugePercentage <= 100 ? "아직 여유가 있어요!" : "권장 칼로리를 초과했어요!";

        // 7) 최종 반환용 Record 조립
        TodayIntakeSummaryResponse.Summary summary = TodayIntakeSummaryResponse.Summary.builder()
                .totalCalories(totalCalories)
                .targetCalories(targetCals)
                .calorieGaugePercentage(calorieGaugePercentage)
                .statusMessage(statusMessage)
                .build();

        TodayIntakeSummaryResponse.Nutrients nutrients = TodayIntakeSummaryResponse.Nutrients.builder()
                .sugarPercentage((int) Math.round((totalSugar / target.targetSugar()) * 100))
                .sodiumPercentage((int) Math.round((totalSodium / target.targetSodium()) * 100))
                .saturatedFatPercentage((int) Math.round((totalSaturatedFat / target.targetSaturatedFat()) * 100))
                .proteinPercentage((int) Math.round((totalProtein / target.targetProtein()) * 100))
                .carbohydratePercentage((int) Math.round((totalCarbohydrate / target.targetCarbohydrate()) * 100))
                .build();

        return TodayIntakeSummaryResponse.builder()
                .date(today.toString())
                .summary(summary)
                .nutrients(nutrients)
                .intakeDetails(intakeDetails)
                .build();
    }

    // 3. 섭취 기록 삭제
    @Transactional
    public void deleteIntakeRecord(Long intakeRecordId) {
        intakeRecordRepository.deleteById(intakeRecordId);
    }
}