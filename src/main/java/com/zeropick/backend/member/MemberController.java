package com.zeropick.backend.member;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users") // 👈 members -> users 로 변경!
public class MemberController {

    // 18번 API: 최근 본 상품 목록 조회 (최종 경로: /api/v1/users/me/recent-products)
    @GetMapping("/me/recent-products")
    public String getRecentProducts() {
        return "최근 본 상품 목록 데이터 반환";
    }
}