package com.zeropick.backend.ingredient.controller;

import com.zeropick.backend.ingredient.dto.IngredientDetailResponse;
import com.zeropick.backend.ingredient.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    // 성분 상세 정보 조회 API
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