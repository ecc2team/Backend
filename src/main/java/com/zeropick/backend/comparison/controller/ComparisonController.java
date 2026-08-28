package com.zeropick.backend.comparison.controller;

import com.zeropick.backend.comparison.dto.ComparisonBoxToggleResponse;
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

    // 안전한 Authentication 주입 객체 파싱
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

    // 1. 내 비교함 목록 조회 (GET /api/v1/comparison-box)
    @GetMapping("/comparison-box")
    public ResponseEntity<Map<String, Object>> getComparisonBox(Authentication authentication) {
        Long userId = extractUserId(authentication);
        ProductCompareResponse data = comparisonService.getComparisonTable(userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 2-A. 비교함 상품 토글 (POST /api/v1/comparison-box/{productId})
    @PostMapping("/comparison-box/{productId}")
    public ResponseEntity<Map<String, Object>> toggleComparisonBoxPath(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        Long userId = extractUserId(authentication);
        ComparisonBoxToggleResponse data = comparisonService.toggleComparisonBox(userId, productId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 토글 처리가 완료되었습니다.",
                "data", data
        ));
    }

    // 2-B. 프론트엔드가 /comparison-box/toggle?productId= 로 보낼 때 지원 (POST /api/v1/comparison-box/toggle)
    @PostMapping("/comparison-box/toggle")
    public ResponseEntity<Map<String, Object>> toggleComparisonBoxQuery(
            Authentication authentication,
            @RequestParam(name = "productId") Long productId
    ) {
        Long userId = extractUserId(authentication);
        ComparisonBoxToggleResponse data = comparisonService.toggleComparisonBox(userId, productId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 토글 처리가 완료되었습니다.",
                "data", data
        ));
    }

    // 2-C. 프론트엔드가 /comparison-box/toggle/{productId} 로 보낼 때 지원 (POST /api/v1/comparison-box/toggle/{productId})
    @PostMapping("/comparison-box/toggle/{productId}")
    public ResponseEntity<Map<String, Object>> toggleComparisonBoxPathToggle(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        Long userId = extractUserId(authentication);
        ComparisonBoxToggleResponse data = comparisonService.toggleComparisonBox(userId, productId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 토글 처리가 완료되었습니다.",
                "data", data
        ));
    }

    // 3. 비교함 상품 삭제 (DELETE /api/v1/comparison-box/{productId})
    @DeleteMapping("/comparison-box/{productId}")
    public ResponseEntity<Map<String, Object>> deleteComparisonBoxProduct(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        Long userId = extractUserId(authentication);
        ComparisonBoxToggleResponse data = comparisonService.deleteComparisonBoxProduct(userId, productId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 상품 삭제가 완료되었습니다.",
                "data", data
        ));
    }
}