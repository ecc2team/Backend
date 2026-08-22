package com.zeropick.backend.product;

import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.ProductPageResponse;
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

    // 인증 토큰 및 안전한 userId 추출 헬퍼 메서드
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalArgumentException("로그인이 필요한 서비스입니다. (인증 토큰 누락)");
        }
        Object principal = authentication.getPrincipal();
        try {
            if (principal instanceof Long) {
                return (Long) principal;
            }
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 유저 식별자입니다.");
        }
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

    // 2. 최근 본 상품 목록 조회 API (GET)
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

    // 3. 최근 본 상품 개별 삭제 API (DELETE)
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

    // 4. 상품 검색 API (GET ?keyword=)
    @GetMapping("/api/v1/products/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(name = "keyword") String keyword
    ) {
        Object data = productService.searchProducts(keyword);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "상품 검색 결과 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 5. 최근 본 상품 기록 저장/갱신 API (POST)
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

    // 6. 전체 상품 리스트 조회 API
    @GetMapping("/api/v1/products")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recommended") String sort
    ) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1에서 100 사이여야 합니다.");
        }

        // Object -> ProductPageResponse 로 확실하게 타입 명시
        ProductPageResponse data = productService.getAllProducts(keyword, page, size, sort);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "전체 상품 리스트 조회가 완료되었습니다.",
                "data", data
        ));
    }
}