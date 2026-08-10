package com.zeropick.backend.product.controller;

import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.RecentProductsResponse;
import com.zeropick.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1. 제품 상세 및 성분 분석 조회 API
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse data = productService.getProductDetail(productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "제품 상세 및 성분 분석 결과 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 2. 최근 본 상품 목록 조회 API
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentProducts(@RequestParam Long userId) {
        RecentProductsResponse data = productService.getRecentProducts(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "최근 본 상품 목록 조회가 완료되었습니다.",
                "data", data
        ));
    }
}