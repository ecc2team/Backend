package com.zeropick.backend.intake.controller;

import com.zeropick.backend.intake.service.IntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1") // 👈 intake와 intakes 경로 혼용을 위해 공통 /api/v1 지정
@RequiredArgsConstructor
public class IntakeController {

    private final IntakeService intakeService;

    // 인증 토큰에서 userId 추출 헬퍼 메서드
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("인증 정보가 존재하지 않습니다.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        return Long.parseLong(principal.toString());
    }

    // 20번 API: 오늘 먹은 제품 기록하기 (POST /api/v1/intake/today)
    @PostMapping("/intake/today")
    public ResponseEntity<Map<String, Object>> addIntakeRecord(
            Authentication authentication,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Long productId
    ) {
        Long userId = extractUserId(authentication);

        // JSON Request Body 또는 Query Param 모두 대응
        Long targetProductId = productId;
        if (targetProductId == null && body != null && body.get("productId") != null) {
            targetProductId = Long.valueOf(body.get("productId").toString());
        }

        intakeService.addIntakeRecord(userId, targetProductId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "섭취 기록이 성공적으로 추가되었습니다."
        ));
    }

    // 21번 API: 오늘의 안전 섭취량 게이지 조회 (GET /api/v1/intake/today)
    @GetMapping("/intake/today")
    public ResponseEntity<Map<String, Object>> getTodayIntakeSummary(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        Map<String, Object> data = intakeService.getTodayIntakeSummary(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "오늘의 섭취량 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 28번 API: 섭취 기록 삭제 (DELETE /api/v1/intakes/{intakeRecordId})
    @DeleteMapping("/intakes/{intakeRecordId}")
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