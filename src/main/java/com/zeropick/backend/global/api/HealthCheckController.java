package com.zeropick.backend.global.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        // 200 OK 상태 코드와 함께 단순 문자열 반환
        return ResponseEntity.ok("OK");
    }
}
