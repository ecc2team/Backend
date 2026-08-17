package com.zeropick.backend.comparison.controller;

import com.zeropick.backend.comparison.dto.ComparisonBoxToggleRequest;
import com.zeropick.backend.comparison.dto.ComparisonBoxToggleResponse;
import com.zeropick.backend.comparison.dto.ProductCompareResponse;
import com.zeropick.backend.comparison.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/comparison-box")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    // 인증 토큰에서 userId를 안전하게 추출하는 헬퍼 메서드
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

    // 1. 비교함 상품 담기/빼기 토글 API
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleComparisonBox(
            Authentication authentication,
            @RequestBody ComparisonBoxToggleRequest request) {
        try {
            Long userId = extractUserId(authentication);
            ComparisonBoxToggleResponse data = comparisonService.toggleComparisonBox(userId, request.getProductId());
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "비교함 상태가 변경되었습니다.",
                    "data", data
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        } catch (IllegalStateException e) {
            if ("COMPARISON_CATEGORY_MISMATCH".equals(e.getMessage())) {
                return ResponseEntity.status(400).body(Map.of(
                        "status", 400,
                        "errorCode", "COMPARISON_CATEGORY_MISMATCH",
                        "message", "같은 카테고리의 상품만 비교할 수 있습니다."
                ));
            }
            throw e;
        }
    }

    // 2. 비교함 상품 삭제 API
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> deleteComparisonBoxProduct(
            Authentication authentication,
            @PathVariable Long productId) {
        try {
            Long userId = extractUserId(authentication);
            ComparisonBoxToggleResponse data = comparisonService.deleteComparisonBoxProduct(userId, productId);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "비교함에서 상품이 삭제되었습니다.",
                    "data", data
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "message", e.getMessage()
            ));
        }
    }

    // 3. 비교함 조회 및 상세 비교 API
    @GetMapping
    public ResponseEntity<Map<String, Object>> getComparisonTable(Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            ProductCompareResponse data = comparisonService.getComparisonTable(userId);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "비교함 조회가 완료되었습니다.",
                    "data", data
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }
}