package com.zeropick.backend.auth;

import com.zeropick.backend.auth.dto.*;
import com.zeropick.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(
            @RequestBody @Valid SignupRequest request,
            @CookieValue(name = CookieUtil.EMAIL_VERIFY_SESSION_COOKIE, required = false) String emailVerifySessionId
    ) {
        SignupResponse response = authService.signup(request, emailVerifySessionId);

        // 가입 성공 후 인증 세션은 재사용되면 안 되므로 쿠키를 지운다. (서버 쪽 레코드는 서비스에서 invalidate)
        ResponseCookie clearedSessionCookie =
                cookieUtil.delete(CookieUtil.EMAIL_VERIFY_SESSION_COOKIE, "/api/v1");

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, clearedSessionCookie.toString())
                .body(ApiResponse.created("회원가입 및 맞춤 취향 설정이 성공적으로 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("로그인에 성공하였습니다.", authService.login(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody @Valid LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("성공적으로 로그아웃되었습니다.", null));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@RequestBody @Valid ReissueRequest request) {
        return ResponseEntity.ok(ApiResponse.success("토큰이 재발급되었습니다.", authService.reissue(request)));
    }
}
