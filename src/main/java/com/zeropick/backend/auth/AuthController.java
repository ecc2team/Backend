package com.zeropick.backend.auth;

import com.zeropick.backend.auth.dto.*;
import com.zeropick.backend.global.response.ApiResponse;
import com.zeropick.backend.global.security.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.zeropick.backend.user.entity.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.zeropick.backend.user.enums.AuthProvider;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    // JwtUtil의 REFRESH_TOKEN_VALIDITY(14일)와 맞춰둔 쿠키 만료 시간
    private static final Duration REFRESH_TOKEN_COOKIE_MAX_AGE = Duration.ofDays(14);

    private final AuthService authService;
    private final CookieUtil cookieUtil;

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
        TokenPair tokenPair = authService.login(request);

        ResponseCookie refreshTokenCookie = cookieUtil.create(
                CookieUtil.REFRESH_TOKEN_COOKIE, tokenPair.refreshToken(), "/api/v1/auth", REFRESH_TOKEN_COOKIE_MAX_AGE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success("로그인에 성공하였습니다.", tokenPair.toResponse()));
    }

    // consumes를 명시해 Content-Type: application/json을 강제한다.
    // body 없이(no @RequestBody) 쿠키만으로 동작하는 엔드포인트라도, 이 제약이 없으면
    // 일반 <form> 자동 제출만으로도 쿠키가 실려서 전송되는 CSRF가 가능해진다.
    // application/json은 CORS "simple request" 조건에 해당하지 않아 프리플라이트를 반드시 거치므로,
    // 허용되지 않은 Origin에서의 요청은 CORS 단계에서 막힌다.
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        authService.logout(refreshToken);

        ResponseCookie clearedRefreshCookie =
                cookieUtil.delete(CookieUtil.REFRESH_TOKEN_COOKIE, "/api/v1/auth");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearedRefreshCookie.toString())
                .body(ApiResponse.success("성공적으로 로그아웃되었습니다.", null));
    }

    @PostMapping(value = "/reissue", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        TokenPair tokenPair = authService.reissue(refreshToken);

        // Refresh Token Rotation: 재발급마다 새 Refresh Token을 다시 Set-Cookie로 내려준다.
        ResponseCookie rotatedRefreshTokenCookie = cookieUtil.create(
                CookieUtil.REFRESH_TOKEN_COOKIE, tokenPair.refreshToken(), "/api/v1/auth", REFRESH_TOKEN_COOKIE_MAX_AGE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, rotatedRefreshTokenCookie.toString())
                .body(ApiResponse.success("토큰이 재발급되었습니다.", tokenPair.toResponse()));
    }

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> kakaoLogin(@RequestBody @Valid SocialLoginRequest request) {
        return socialLogin(AuthProvider.KAKAO, request.authCode());
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> googleLogin(@RequestBody @Valid SocialLoginRequest request) {
        return socialLogin(AuthProvider.GOOGLE, request.authCode());
    }

    private ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(AuthProvider provider, String authCode) {
        SocialLoginResult result = authService.socialLogin(provider, authCode);

        ResponseCookie refreshTokenCookie = cookieUtil.create(
                CookieUtil.REFRESH_TOKEN_COOKIE, result.refreshToken(), "/api/v1/auth", REFRESH_TOKEN_COOKIE_MAX_AGE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success("소셜 로그인에 성공하였습니다.", result.toResponse()));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<OnboardingResponse>> onboarding(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid OnboardingRequest request
    ) {
        OnboardingResponse response = authService.updateOnboarding(user, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success("온보딩이 완료되었습니다.", response));
    }
}