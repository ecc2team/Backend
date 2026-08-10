package com.zeropick.backend.ingredient;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

    // 15번 API: 특정 성분 사전 상세 조회
    @GetMapping("/{code}")
    public String getIngredientDetail(@PathVariable String code) {
        return "성분 상세 정보 반환 (코드: " + code + ")";
    }
}