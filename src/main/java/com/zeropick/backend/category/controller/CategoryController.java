package com.zeropick.backend.category.controller;

import com.zeropick.backend.category.dto.CategoryBestProductResponse;
import com.zeropick.backend.category.dto.CategoryProductListResponse;
import com.zeropick.backend.category.dto.CategoryResponse;
import com.zeropick.backend.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCategories() {
        List<CategoryResponse> data = categoryService.getAllCategories();
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "카테고리 목록 조회 성공",
                "data", data
        ));
    }

    @GetMapping("/{category}/best")
    public ResponseEntity<Map<String, Object>> getBestProducts(
            @PathVariable String category,
            @RequestParam(defaultValue = "5") int size
    ) {
        List<CategoryBestProductResponse> data = categoryService.getBestProducts(category, size);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "카테고리 베스트 랭킹 조회가 성공적으로 완료되었습니다.",
                "data", data
        ));
    }

    @GetMapping("/{category}/products")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recommended") String sort // 기본값 처리
    ) {
        // size 범위 제한 (1~100)
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1에서 100 사이여야 합니다.");
        }

        // sort 값 검증
        CategoryProductListResponse data = categoryService.getProducts(category, keyword, page, size, sort);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "제품 검색 리스트 조회가 성공적으로 완료되었습니다.",
                "data", data
        ));
    }
}