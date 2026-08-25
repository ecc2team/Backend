package com.zeropick.backend.intake.controller;

import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.global.response.ApiResponse;
import com.zeropick.backend.intake.dto.IntakeCreateRequest;
import com.zeropick.backend.intake.dto.TodayIntakeSummaryResponse;
import com.zeropick.backend.intake.service.IntakeService;
import com.zeropick.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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

    // 오늘 먹은 제품 기록하기
    @PostMapping("/intakes")
    public ResponseEntity<ApiResponse<Void>> addIntakeRecord(
            Authentication authentication,
            @RequestBody IntakeCreateRequest request
    ) {
        Long userId = extractUserId(authentication);

        BigDecimal quantity = request.quantity() != null
                ? BigDecimal.valueOf(request.quantity())
                : BigDecimal.ONE;

        intakeService.addIntakeRecord(userId, request.productId(), quantity);

        return ResponseEntity.ok(ApiResponse.success("섭취 기록이 성공적으로 추가되었습니다.", null));
    }

    // 오늘의 안전 섭취량 게이지 조회
    @GetMapping("/intakes/today")
    public ResponseEntity<ApiResponse<TodayIntakeSummaryResponse>> getTodayIntakeSummary(
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        TodayIntakeSummaryResponse data = intakeService.getTodayIntakeSummary(userId);

        return ResponseEntity.ok(ApiResponse.success("오늘의 섭취량 조회가 완료되었습니다.", data));
    }

    // 섭취 기록 삭제
    @DeleteMapping("/intakes/{intakeRecordId}")
    public ResponseEntity<ApiResponse<Void>> deleteIntakeRecord(
            @PathVariable("intakeRecordId") Long intakeRecordId
    ) {
        intakeService.deleteIntakeRecord(intakeRecordId);
        return ResponseEntity.ok(ApiResponse.success("섭취 기록이 성공적으로 삭제되었습니다.", null));
    }
}