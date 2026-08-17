package com.zeropick.backend.intake.service;

import com.zeropick.backend.intake.entity.IntakeRecord;
import com.zeropick.backend.intake.repository.IntakeRecordRepository;
import com.zeropick.backend.product.entity.Product;
import com.zeropick.backend.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntakeService {

    private final IntakeRecordRepository intakeRecordRepository;
    private final ProductRepository productRepository;

    // 1. 오늘 먹은 제품 기록 추가 (20번 API)
    @Transactional
    public void addIntakeRecord(Long userId, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("기록할 상품 ID(productId)가 전달되지 않았증니다.");
        }

        // DB에 존재하지 않는 상품 ID 요청 시 예외 처리
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. id=" + productId));

        IntakeRecord record = new IntakeRecord(userId, product.getId(), LocalDate.now());
        intakeRecordRepository.save(record);
    }

    // 2. 오늘의 섭취량 조회 및 계산 (21번 API)
    public Map<String, Object> getTodayIntakeSummary(Long userId) {
        List<IntakeRecord> todayRecords = intakeRecordRepository.findByUserIdAndIntakeDate(userId, LocalDate.now());

        int totalCalories = 0;
        double totalSugar = 0.0;
        double totalSodium = 0.0;

        for (IntakeRecord record : todayRecords) {
            Product product = productRepository.findById(record.getProductId()).orElse(null);
            if (product != null) {
                totalCalories += product.getCalories() != null ? product.getCalories() : 0;
                totalSugar += product.getSugar() != null ? product.getSugar().doubleValue() : 0.0;
                totalSodium += product.getSodium() != null ? product.getSodium().doubleValue() : 0.0;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("intakeCount", todayRecords.size());
        result.put("totalCalories", totalCalories);
        result.put("totalSugar", totalSugar);
        result.put("totalSodium", totalSodium);

        return result;
    }

    // 3. 섭취 기록 삭제 (28번 API)
    @Transactional
    public void deleteIntakeRecord(Long intakeRecordId) {
        if (!intakeRecordRepository.existsById(intakeRecordId)) {
            throw new IllegalArgumentException("존재하지 않는 섭취 기록입니다. id=" + intakeRecordId);
        }
        intakeRecordRepository.deleteById(intakeRecordId);
    }
}