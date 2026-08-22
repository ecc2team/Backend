package com.zeropick.backend.product;

import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.product.dto.ProductDetailResponse;
import com.zeropick.backend.product.dto.ProductPageResponse;
import com.zeropick.backend.product.dto.RecentProductsResponse;
import com.zeropick.backend.product.service.ProductService;
import com.zeropick.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // UserId 추출
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

    // 제품 상세 및 성분 분석 조회
    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse data = productService.getProductDetail(productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "제품 상세 및 성분 분석 결과 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 최근 본 상품 목록 조회
    @GetMapping("/users/me/recent-products")
    public ResponseEntity<Map<String, Object>> getRecentProducts(Authentication authentication) {
        Long userId = extractUserId(authentication);
        RecentProductsResponse data = productService.getRecentProducts(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "최근 본 상품 목록 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 최근 본 상품 저장
    @PostMapping("/users/me/recent-products/{productId}")
    public ResponseEntity<Map<String, Object>> saveRecentProduct(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        Long userId = extractUserId(authentication);
        productService.saveRecentProduct(userId, productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "최근 본 상품이 저장되었습니다."
        ));
    }

    // 최근 본 상품 개별 삭제
    @DeleteMapping("/users/me/recent-products/{productId}")
    public ResponseEntity<Map<String, Object>> deleteRecentProduct(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        Long userId = extractUserId(authentication);
        productService.deleteRecentProduct(userId, productId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "최근 본 상품이 삭제되었습니다."
        ));
    }

    // 상품 검색
    @GetMapping("/products/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String keyword) {
        List<ProductDetailResponse> data = productService.searchProducts(keyword);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "상품 검색 결과 조회가 완료되었습니다.",
                "data", data
        ));
    }

    // 전체 상품 리스트 조회
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recommended") String sort
    ) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1에서 100 사이여야 합니다.");
        }

        ProductPageResponse data = productService.getAllProducts(keyword, page, size, sort);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "전체 상품 리스트 조회가 완료되었습니다.",
                "data", data
        ));
    }
}