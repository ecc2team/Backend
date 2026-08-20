package com.zeropick.backend.intake.controller;

import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.intake.dto.TodayIntakeSummaryResponse;
import com.zeropick.backend.intake.service.IntakeService;
import com.zeropick.backend.user.entity.User; 
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1") 
@RequiredArgsConstructor
public class IntakeController {

    private final IntakeService intakeService;

    private Long extractUserId(Authentication authentication) {
    if (authentication == null || authentication.getPrincipal() == null) {
        throw new UnauthorizedException("인증 정보가 존재하지 않습니다.");
    }
    
    Object principal = authentication.getPrincipal();
    
    if (principal instanceof User) {
        return ((User) principal).getId();
    }
    
    throw new UnauthorizedException("유효하지 않은 인증 토큰 형태입니다.");
}

    // 20번 API: 오늘 먹은 제품 기록하기 (POST /api/v1/intake/today)
    @PostMapping("/intake/today")
    public ResponseEntity<Map<String, Object>> addIntakeRecord(
            Authentication authentication,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) BigDecimal quantity
    ) {
        Long userId = extractUserId(authentication);

        // productId 세팅 (JSON Body 대응)
        Long targetProductId = productId;
        if (targetProductId == null && body != null && body.get("productId") != null) {
            targetProductId = Long.valueOf(body.get("productId").toString());
        }

        // quantity 세팅 (JSON Body 대응)
        BigDecimal targetQuantity = quantity;
        if (targetQuantity == null && body != null && body.get("quantity") != null) {
            targetQuantity = new BigDecimal(body.get("quantity").toString());
        }

        // 서비스 호출 (수량 포함)
        intakeService.addIntakeRecord(userId, targetProductId, targetQuantity);

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
        TodayIntakeSummaryResponse data = intakeService.getTodayIntakeSummary(userId);

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