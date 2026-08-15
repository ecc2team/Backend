package com.zeropick.backend.auth;

import com.zeropick.backend.auth.dto.LoginRequest;
import com.zeropick.backend.auth.dto.OnboardingRequest;
import com.zeropick.backend.auth.dto.SignupRequest;
import com.zeropick.backend.auth.dto.SignupResponse;
import com.zeropick.backend.auth.dto.TokenPair;
import com.zeropick.backend.category.Category;
import com.zeropick.backend.category.CategoryRepository;
import com.zeropick.backend.email.EmailVerificationService;
import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.global.security.JwtUtil;
import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.repository.UserAllergyRepository;
import com.zeropick.backend.user.repository.UserPreferredCategoryRepository;
import com.zeropick.backend.user.repository.UserPreferredIngredientRepository;
import com.zeropick.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserAllergyRepository userAllergyRepository;
    @Mock private UserPreferredIngredientRepository userPreferredIngredientRepository;
    @Mock private UserPreferredCategoryRepository userPreferredCategoryRepository;
    @Mock private IngredientRepository ingredientRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthService authService;

    private static final String SESSION_ID = "test-session-id";

    private SignupRequest signupRequest(String email) {
        OnboardingRequest onboarding = new OnboardingRequest(
                List.of("DRINK", "SNACK"),
                List.of("MALTITOL"),
                List.of("MILK")
        );
        return new SignupRequest(email, "securepassword123!", "제로러버", onboarding);
    }

    private Category category(String code) {
        Category category = new Category();
        ReflectionTestUtils.setField(category, "code", code);
        return category;
    }

    private Ingredient ingredient(String code) {
        Ingredient ingredient = new Ingredient();
        ReflectionTestUtils.setField(ingredient, "code", code);
        return ingredient;
    }

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("이메일 인증 세션이 유효하지 않으면 가입에 실패한다")
        void signup_fails_when_email_not_verified() {
            willThrow(new IllegalStateException("이메일 인증이 필요합니다."))
                    .given(emailVerificationService).assertSessionVerified(anyString(), anyString());

            assertThatThrownBy(() -> authService.signup(signupRequest("zerolover@naver.com"), SESSION_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이메일 인증이 필요합니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("이미 가입된 이메일이면 가입에 실패한다")
        void signup_fails_when_email_already_registered() {
            given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(true);

            assertThatThrownBy(() -> authService.signup(signupRequest("zerolover@naver.com"), SESSION_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 가입된 이메일입니다.");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("인증된 신규 이메일이면 가입에 성공하고 온보딩 데이터를 저장한다")
        void signup_succeeds_and_saves_onboarding() {
            String email = "zerolover@naver.com";
            given(userRepository.existsByEmailAndDeletedAtIsNull(email)).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encoded-password");

            given(categoryRepository.findAllByCodeIn(List.of("DRINK", "SNACK")))
                    .willReturn(List.of(category("DRINK"), category("SNACK")));
            given(ingredientRepository.findAllByCodeIn(List.of("MALTITOL")))
                    .willReturn(List.of(ingredient("MALTITOL")));
            given(ingredientRepository.findAllByCodeIn(List.of("MILK")))
                    .willReturn(List.of(ingredient("MILK")));

            SignupResponse response = authService.signup(signupRequest(email), SESSION_ID);

            assertThat(response.email()).isEqualTo(email);
            assertThat(response.nickname()).isEqualTo("제로러버");

            verify(emailVerificationService).assertSessionVerified(SESSION_ID, email);
            verify(userRepository).save(any(User.class));
            verify(userPreferredCategoryRepository, times(2)).save(any());
            verify(userPreferredIngredientRepository, times(1)).save(any());
            verify(userAllergyRepository, times(1)).save(any());
            verify(emailVerificationService).invalidate(email);
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("존재하지 않는 이메일이면 로그인에 실패한다")
        void login_fails_when_user_not_found() {
            given(userRepository.findByEmailAndDeletedAtIsNull(anyString()))
                    .willReturn(Optional.empty());

            LoginRequest request = new LoginRequest("nouser@naver.com", "securepassword123!");

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
        void login_fails_when_password_mismatch() {
            User user = User.builder()
                    .email("zerolover@naver.com")
                    .password("encoded-password")
                    .nickname("제로러버")
                    .build();

            given(userRepository.findByEmailAndDeletedAtIsNull(anyString()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            LoginRequest request = new LoginRequest("zerolover@naver.com", "wrongpassword");

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        @Test
        @DisplayName("이메일/비밀번호가 맞으면 accessToken과 refreshToken을 발급하고 refreshToken을 저장한다")
        void login_succeeds_and_issues_tokens() {
            User user = User.builder()
                    .email("zerolover@naver.com")
                    .password("encoded-password")
                    .nickname("제로러버")
                    .build();

            given(userRepository.findByEmailAndDeletedAtIsNull(anyString()))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(jwtUtil.generateToken(anyString())).willReturn("access-token");
            given(jwtUtil.generateRefreshToken(anyString())).willReturn("refresh-token");

            TokenPair response = authService.login(
                    new LoginRequest("zerolover@naver.com", "securepassword123!"));

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(user.getRefreshToken()).isEqualTo("refresh-token");
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        @Test
        @DisplayName("쿠키에 refreshToken이 없으면 401에 해당하는 예외가 발생한다")
        void reissue_fails_when_cookie_missing() {
            assertThatThrownBy(() -> authService.reissue(null))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("refreshToken이 무효하면 401에 해당하는 예외가 발생한다")
        void reissue_fails_when_token_invalid() {
            given(jwtUtil.isValid("invalid-refresh-token")).willReturn(false);

            assertThatThrownBy(() -> authService.reissue("invalid-refresh-token"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("DB에 저장된 refreshToken과 다르면 401에 해당하는 예외가 발생한다")
        void reissue_fails_when_token_mismatch() {
            User user = User.builder()
                    .email("zerolover@naver.com")
                    .password("encoded-password")
                    .nickname("제로러버")
                    .build();
            ReflectionTestUtils.setField(user, "refreshToken", "stored-refresh-token");

            given(jwtUtil.isValid("stale-refresh-token")).willReturn(true);
            given(jwtUtil.extractEmail("stale-refresh-token")).willReturn(user.getEmail());
            given(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
                    .willReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.reissue("stale-refresh-token"))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        @DisplayName("유효한 refreshToken이면 새 토큰 쌍을 발급하고 저장된 refreshToken을 회전시킨다")
        void reissue_succeeds_and_rotates_refresh_token() {
            User user = User.builder()
                    .email("zerolover@naver.com")
                    .password("encoded-password")
                    .nickname("제로러버")
                    .build();
            ReflectionTestUtils.setField(user, "refreshToken", "old-refresh-token");

            given(jwtUtil.isValid("old-refresh-token")).willReturn(true);
            given(jwtUtil.extractEmail("old-refresh-token")).willReturn(user.getEmail());
            given(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
                    .willReturn(Optional.of(user));
            given(jwtUtil.generateToken(user.getEmail())).willReturn("new-access-token");
            given(jwtUtil.generateRefreshToken(user.getEmail())).willReturn("new-refresh-token");

            TokenPair result = authService.reissue("old-refresh-token");

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(user.getRefreshToken()).isEqualTo("new-refresh-token");
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("쿠키에 refreshToken이 없으면 조용히 종료한다")
        void logout_noop_when_cookie_missing() {
            authService.logout(null);

            verify(userRepository, never()).findByEmailAndDeletedAtIsNull(anyString());
        }

        @Test
        @DisplayName("유효한 refreshToken이면 저장된 refreshToken을 무효화한다")
        void logout_clears_stored_refresh_token() {
            User user = User.builder()
                    .email("zerolover@naver.com")
                    .password("encoded-password")
                    .nickname("제로러버")
                    .build();
            ReflectionTestUtils.setField(user, "refreshToken", "current-refresh-token");

            given(jwtUtil.isValid("current-refresh-token")).willReturn(true);
            given(jwtUtil.extractEmail("current-refresh-token")).willReturn(user.getEmail());
            given(userRepository.findByEmailAndDeletedAtIsNull(user.getEmail()))
                    .willReturn(Optional.of(user));

            authService.logout("current-refresh-token");

            assertThat(user.getRefreshToken()).isNull();
        }
    }
}