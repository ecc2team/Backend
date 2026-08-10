package com.zeropick.backend.comparison.controller;

import com.zeropick.backend.comparison.dto.ProductCompareResponse;
import com.zeropick.backend.comparison.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/comparison-boxes")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    // 1. 비교함 제품 추가 API
    @PostMapping
    public ResponseEntity<Map<String, Object>> addComparisonBox(@RequestParam Long userId, @RequestParam Long productId) {
        comparisonService.addComparisonBox(userId, productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함에 제품이 추가되었습니다."
        ));
    }

    // 2. 비교함 조회 및 상세 비교 API
    @GetMapping
    public ResponseEntity<Map<String, Object>> getComparisonTable(@RequestParam Long userId) {
        ProductCompareResponse data = comparisonService.getComparisonTable(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 3. 비교함 제품 삭제 API
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteComparisonBox(@RequestParam Long userId, @PathVariable Long productId) {
        comparisonService.deleteComparisonBox(userId, productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비교함에서 제품이 삭제되었습니다."
        ));
    }
}