package com.zeropick.backend.product;

import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.RecentProductsResponse;
import com.zeropick.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 인증 토큰에서 userId 추출하는 헬퍼 메서드
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

    // 1. 제품 상세 및 성분 분석 조회 API
    @GetMapping("/api/v1/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse data = productService.getProductDetail(productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "제품 상세 및 성분 분석 결과 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 2. 최근 본 상품 목록 조회 API
    @GetMapping("/api/v1/users/me/recent-products")
    public ResponseEntity<Map<String, Object>> getRecentProducts(Authentication authentication) {
        Long userId = extractUserId(authentication);
        RecentProductsResponse data = productService.getRecentProducts(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "최근 본 상품 목록 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 3. 최근 본 상품 개별 삭제 API
    @DeleteMapping("/api/v1/users/me/recent-products/{productId}")
    public ResponseEntity<Map<String, Object>> deleteRecentProduct(
            Authentication authentication,
            @PathVariable Long productId) {
        Long userId = extractUserId(authentication);
        productService.deleteRecentProduct(userId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "최근 본 상품이 삭제되었습니다.");
        response.put("data", null);

        return ResponseEntity.ok(response);
    }

    // 4. 상품 검색 API
    @GetMapping("/api/v1/products/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(name = "query") String query
    ) {
        Object data = productService.searchProducts(query);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "상품 검색 결과 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 5. 최근 본 상품 기록 저장/갱신 API
    @PostMapping("/api/v1/users/me/recent-products/{productId}")
    public ResponseEntity<Map<String, Object>> saveRecentProduct(
            Authentication authentication,
            @PathVariable Long productId) {
        Long userId = extractUserId(authentication);
        productService.saveRecentProduct(userId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "최근 본 상품 기록이 저장되었습니다.");
        response.put("data", null);

        return ResponseEntity.ok(response);
    }
}