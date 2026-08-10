package com.zeropick.backend.comparison.controller;

import com.zeropick.backend.comparison.dto.ComparisonToggleRequest;
import com.zeropick.backend.comparison.dto.ComparisonToggleResponse;
import com.zeropick.backend.comparison.dto.ProductCompareResponse;
import com.zeropick.backend.comparison.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/comparison-box")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    // 1. 비교함 상품 담기/빼기 토글 API
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleComparisonBox(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestBody ComparisonToggleRequest request) {
        try {
            ComparisonToggleResponse data = comparisonService.toggleComparisonBox(userId, request.getProductId());
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "비교함 상태가 변경되었습니다.",
                    "data", data
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
            @RequestParam(defaultValue = "1") Long userId,
            @PathVariable Long productId) {
        try {
            ComparisonToggleResponse data = comparisonService.deleteComparisonBoxProduct(userId, productId);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "비교함에서 상품이 삭제되었습니다.",
                    "data", data
            ));
        } catch (IllegalArgumentException e) {
            if ("NOT_FOUND_IN_COMPARISON_BOX".equals(e.getMessage())) {
                return ResponseEntity.status(404).body(Map.of(
                        "status", 404,
                        "errorCode", "NOT_FOUND_IN_COMPARISON_BOX",
                        "message", "비교함에 해당 상품이 존재하지 않습니다."
                ));
            }
            throw e;
        }
    }

    // 3. 비교함 조회 및 상세 비교 API
    @GetMapping
    public ResponseEntity<Map<String, Object>> getComparisonTable(@RequestParam(defaultValue = "1") Long userId) {
        ProductCompareResponse data = comparisonService.getComparisonTable(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 조회가 완료되었습니다.",
                "data", data
        ));
    }
}