package com.zeropick.backend.user;

import com.zeropick.backend.global.response.ApiResponse;
import com.zeropick.backend.user.dto.EmailCheckResponse;
import com.zeropick.backend.user.dto.FindAccountRequest;
import com.zeropick.backend.user.dto.FindAccountResponse;
import com.zeropick.backend.user.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/find-account")
    public ResponseEntity<ApiResponse<FindAccountResponse>> findAccount(@RequestBody @Valid FindAccountRequest request){
        FindAccountResponse response = userService.findAccount(request.email());
        return ResponseEntity.ok(ApiResponse.success("가입된 계정 조회가 완료되었습니다.", response));
    }

    @PostMapping("/reset-pw")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request){
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경이 정상적으로 처리되었습니다.", null));
    }
}
