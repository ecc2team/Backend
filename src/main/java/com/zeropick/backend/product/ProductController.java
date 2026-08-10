package com.zeropick.backend.product;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    // 13번 API: 제품 상세 정보 및 성분 분석 결과 조회
    @GetMapping("/{productId}")
    public String getProductDetail(@PathVariable Long productId) {
        return "제품 상세 및 성분 분석 결과 반환 (ID: " + productId + ")";
    }

    // 14번 API: 선택 제품 다중 비교 데이터 조회 (최대 3개)
    @GetMapping("/compare")
    public String compareProducts(@RequestParam List<Long> productIds) {
        return "제품 다중 비교 데이터 반환 (IDs: " + productIds + ")";
    }
}