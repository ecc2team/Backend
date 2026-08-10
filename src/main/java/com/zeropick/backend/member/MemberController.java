package com.zeropick.backend.member;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    // 18번 API: 최근 본 상품 목록 조회
    @GetMapping("/me/recent-products")
    public String getRecentProducts() {
        return "최근 본 상품 목록 데이터 반환";
    }
}