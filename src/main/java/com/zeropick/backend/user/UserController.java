package com.zeropick.backend.user;

import com.zeropick.backend.global.response.ApiResponse;
import com.zeropick.backend.user.dto.EmailCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<EmailCheckResponse>> checkEmail(@RequestParam String email){
        EmailCheckResponse response = userService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success("이메일 중복 확인 결과입니다.", response));
    }
}
