package com.zeropick.backend.intake.controller;

import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.intake.dto.IntakeRecordRequest;
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
        if (principal instanceof Long) {
            return (Long) principal;
        }
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("유효하지 않은 인증 토큰 형태입니다.");
        }
    }

    // 20번 API: 오늘 먹은 제품 기록하기 (POST /api/v1/intake/today)
    @PostMapping("/intake/today")
    public ResponseEntity<Map<String, Object>> addIntakeRecord(
            Authentication authentication,
            @RequestBody(required = false) IntakeRecordRequest requestDto,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) BigDecimal quantity
    ) {
        Long userId = extractUserId(authentication);

        // RequestBody(DTO) 우선 추출, 없을 경우 QueryParam에서 추출
        Long targetProductId = (requestDto != null && requestDto.getProductId() != null)
                ? requestDto.getProductId() : productId;
        BigDecimal targetQuantity = (requestDto != null && requestDto.getQuantity() != null)
                ? requestDto.getQuantity() : quantity;

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

    // 28번 API: 섭취 기록 삭제 (명세서 표준 단일 경로로 통일)
    @DeleteMapping("/intake/records/{intakeRecordId}")
    public ResponseEntity<Map<String, Object>> deleteIntakeRecord(
            @PathVariable("intakeRecordId") String intakeRecordId
    ) {
        intakeService.deleteIntakeRecord(intakeRecordId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "섭취 기록이 성공적으로 삭제되었습니다."
        ));
    }
}