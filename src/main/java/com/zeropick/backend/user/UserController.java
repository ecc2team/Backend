package com.zeropick.backend.user;

import com.zeropick.backend.global.response.ApiResponse;
import com.zeropick.backend.global.security.CookieUtil;
import com.zeropick.backend.user.dto.*;
import com.zeropick.backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<EmailCheckResponse>> checkEmail(@RequestParam String email) {
        EmailCheckResponse response = userService.checkEmail(email);
        return ResponseEntity.ok(ApiResponse.success("이메일 중복 확인 결과입니다.", response));
    }

    // 닉네임 중복 확인: 회원가입 폼 실시간 검증 + 마이페이지 프로필 수정 시 재사용
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<NicknameCheckResponse>> checkNickname(@RequestParam String nickname) {
        NicknameCheckResponse response = userService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResponse.success("닉네임 중복 확인 결과입니다.", response));
    }

    @PostMapping("/find-account")
    public ResponseEntity<ApiResponse<FindAccountResponse>> findAccount(@RequestBody @Valid FindAccountRequest request) {
        FindAccountResponse response = userService.findAccount(request.email());
        return ResponseEntity.ok(ApiResponse.success("가입된 계정 조회가 완료되었습니다.", response));
    }

    @PostMapping("/reset-pw")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request, @CookieValue(name = CookieUtil.EMAIL_VERIFY_SESSION_COOKIE, required = false) String emailVerifySessionId
    ) {
        userService.resetPassword(request, emailVerifySessionId);
        ResponseCookie clearedSessionCookie = cookieUtil.delete(CookieUtil.EMAIL_VERIFY_SESSION_COOKIE, "/api/v1");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearedSessionCookie.toString())
                .body(ApiResponse.success("비밀번호 변경이 정상적으로 처리되었습니다.", null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal User user) {
        userService.withdraw(user.getId());
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 정상적으로 처리되었습니다.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        UserProfileResponse response = userService.getMyProfile(user);
        return ResponseEntity.ok(ApiResponse.success("프로필 조회가 성공적으로 완료되었습니다.", response));
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> updatePreferences(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid UserPreferencesRequest request
    ) {
        UserPreferencesResponse response = userService.updatePreferences(user, request);
        return ResponseEntity.ok(ApiResponse.success("취향 설정이 성공적으로 업데이트되었습니다.", response));
    }
}
