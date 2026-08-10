package com.zeropick.backend.intake;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/intake")
public class IntakeController {

    // 16번 API: 오늘 먹은 제품 기록하기 API
    @PostMapping("/today")
    public String recordTodayIntake() {
        return "오늘 먹은 제품 기록 완료";
    }

    // 17번 API: 오늘의 안전 섭취량 게이지 조회 API
    @GetMapping("/today")
    public String getTodayIntakeGauge() {
        return "오늘의 안전 섭취량 게이지 데이터 반환";
    }
}