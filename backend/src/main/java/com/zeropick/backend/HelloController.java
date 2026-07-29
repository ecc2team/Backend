package com.zeropick.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "🎉 백엔드 기본 세팅 및 localhost 연결이 완료되었습니다! 🎉";
    }
}