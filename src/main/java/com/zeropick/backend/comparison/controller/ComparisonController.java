package com.zeropick.backend.comparison.controller;

import com.zeropick.backend.comparison.dto.ProductCompareResponse;
import com.zeropick.backend.comparison.service.ComparisonService;
import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    // 안전한 Authentication 주입 객체 파싱 (User@... 400 에러 해결)
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("인증 정보가 존재하지 않습니다.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }
        if (principal instanceof Long id) {
            return id;
        }
        if (principal instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                throw new UnauthorizedException("유효하지 않은 유저 식별자입니다.");
            }
        }
        throw new UnauthorizedException("유효하지 않은 인증 토큰 형태입니다.");
    }

    // 내 비교함 목록 조회 (GET /api/v1/comparison-box)
    @GetMapping("/comparison-box")
    public ResponseEntity<Map<String, Object>> getComparisonBox(Authentication authentication) {
        Long userId = extractUserId(authentication);

        // Service의 getComparisonTable 메서드 호출
        ProductCompareResponse data = comparisonService.getComparisonTable(userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 조회가 완료되었습니다.",
                "data", data
        ));
    }
}