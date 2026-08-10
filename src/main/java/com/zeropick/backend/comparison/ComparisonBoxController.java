package com.zeropick.backend.comparison;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comparison-box")
public class ComparisonBoxController {

    // 19번 API: 비교함에 상품 담기/빼기
    @PostMapping("/toggle")
    public String toggleComparisonBox() {
        return "비교함 상태 변경 완료 (담기/빼기)";
    }

    // 20번 API: 내 비교함 목록 조회
    @GetMapping
    public String getComparisonBoxList() {
        return "내 비교함 목록 데이터 반환";
    }
}