package com.zeropick.backend.ingredient.controller;

import com.zeropick.backend.ingredient.dto.IngredientDetailResponse;
import com.zeropick.backend.ingredient.dto.IngredientResponse;
import com.zeropick.backend.ingredient.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // 1. 성분 마스터 데이터 전체 목록 조회 API (신규)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllIngredients() {
        List<IngredientResponse> data = ingredientService.getAllIngredients();
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "성분 목록 조회 성공",
                "data", data
        ));
    }

    // 2. 단일 성분 상세 조회 API
    @GetMapping("/{code}")
    public ResponseEntity<Map<String, Object>> getIngredientDetail(@PathVariable String code) {
        IngredientDetailResponse data = ingredientService.getIngredientDetail(code);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "성분 상세 정보 조회가 완료되었습니다.",
                "data", data
        ));
    }
}