package com.zeropick.backend.intake.controller;

import com.zeropick.backend.intake.service.IntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/intakes") // 👈 1. intake-records -> intakes 로 변경!
@RequiredArgsConstructor
public class IntakeController {

    private final IntakeService intakeService;

    // 1. 섭취 기록 추가 API
    @PostMapping
    public ResponseEntity<Map<String, Object>> addIntakeRecord(
            @RequestParam Long userId,
            @RequestParam Long productId
    ) {
        intakeService.addIntakeRecord(userId, productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "섭취 기록이 성공적으로 추가되었습니다."
        ));
    }

    // 2. 오늘의 섭취량 조회 API
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayIntakeSummary(
            @RequestParam Long userId
    ) {
        Map<String, Object> data = intakeService.getTodayIntakeSummary(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "오늘의 섭취량 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 3. 섭취 기록 삭제 API (명세서 규격 반영 추가!)
    @DeleteMapping("/{intakeRecordId}")
    public ResponseEntity<Map<String, Object>> deleteIntakeRecord(
            @PathVariable Long intakeRecordId
    ) {
        intakeService.deleteIntakeRecord(intakeRecordId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "섭취 기록이 성공적으로 삭제되었습니다."
        ));
    }
}