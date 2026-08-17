package com.zeropick.backend.auth;

import com.zeropick.backend.auth.dto.*;
import com.zeropick.backend.category.CategoryRepository;
import com.zeropick.backend.email.EmailVerificationService;
import com.zeropick.backend.global.exception.UnauthorizedException;
import com.zeropick.backend.global.security.JwtUtil;
import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import com.zeropick.backend.category.Category;
import com.zeropick.backend.user.*;
import com.zeropick.backend.user.entity.User;
import com.zeropick.backend.user.entity.UserAllergy;
import com.zeropick.backend.user.entity.UserPreferredCategory;
import com.zeropick.backend.user.entity.UserPreferredIngredient;
import com.zeropick.backend.user.repository.UserAllergyRepository;
import com.zeropick.backend.user.repository.UserPreferredCategoryRepository;
import com.zeropick.backend.user.repository.UserPreferredIngredientRepository;
import com.zeropick.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.zeropick.backend.auth.oauth.SocialOAuthClient;
import com.zeropick.backend.auth.oauth.SocialUserInfo;
import java.util.List;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final UserPreferredIngredientRepository userPreferredIngredientRepository;
    private final UserPreferredCategoryRepository userPreferredCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final List<SocialOAuthClient> socialOAuthClients;

    @Transactional
    public SignupResponse signup(SignupRequest request, String emailVerifySessionId) {
        emailVerificationService.assertSessionVerified(emailVerifySessionId, request.email());
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);

        saveOnboarding(user, request.onboarding());

        emailVerificationService.invalidate(request.email());

        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname());
    }

    private void saveOnboarding(User user, com.zeropick.backend.auth.dto.OnboardingRequest onboarding) {
        List<Category> categories = categoryRepository.findAllByCodeIn(onboarding.preferredCategories());
        categories.forEach(category -> userPreferredCategoryRepository.save(
                UserPreferredCategory.builder().user(user).category(category).build()
        ));

        List<Ingredient> disliked = ingredientRepository.findAllByCodeIn(onboarding.dislikedIngredients());
        disliked.forEach(ingredient -> userPreferredIngredientRepository.save(
                UserPreferredIngredient.builder().user(user).ingredient(ingredient).build()
        ));

        List<Ingredient> allergies = ingredientRepository.findAllByCodeIn(onboarding.allergyFlags());
        allergies.forEach(ingredient -> userAllergyRepository.save(
                UserAllergy.builder().user(user).ingredient(ingredient).build()
        ));
    }

    public TokenPair login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken);

        return new TokenPair(user.getId(), accessToken, refreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (!StringUtils.hasText(refreshToken) || !jwtUtil.isValid(refreshToken)) {
            return;
        }

        String email = jwtUtil.extractEmail(refreshToken);
        userRepository.findByEmailAndDeletedAtIsNull(email)
                        .filter(user -> refreshToken.equals(user.getRefreshToken()))
                                .ifPresent(user -> user.updateRefreshToken(null));
    }

    @Transactional
    public TokenPair reissue(String refreshToken) {

        if (!StringUtils.hasText(refreshToken) || !jwtUtil.isValid(refreshToken)) {
            throw new UnauthorizedException("리프레시 토큰이 유효하지 않습니다. 다시 로그인해주세요.");
        }

        String email = jwtUtil.extractEmail(refreshToken);

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UnauthorizedException("리프레시 토큰이 유효하지 않습니다. 다시 로그인해주세요."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new UnauthorizedException("리프레시 토큰이 유효하지 않습니다. 다시 로그인해주세요.");
        }

        String newAccessToken = jwtUtil.generateToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(newRefreshToken);

        return new TokenPair(user.getId(), newAccessToken, newRefreshToken);
    }

    @Transactional
    public SocialLoginResult socialLogin(AuthProvider provider, String authCode) {
        SocialUserInfo userInfo = resolveClient(provider).getUserInfo(authCode);

        var existingUser = userRepository.findByEmailAndDeletedAtIsNull(userInfo.email());
        boolean isNewUser = existingUser.isEmpty();

        User user = existingUser.orElseGet(() -> userRepository.save(
                User.builder()
                        .email(userInfo.email())
                        .nickname(resolveNickname(userInfo))
                        .provider(provider)
                        .build()
        ));

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken);

        return new SocialLoginResult(user.getId(), isNewUser, accessToken, refreshToken);
    }

    private SocialOAuthClient resolveClient(AuthProvider provider) {
        return socialOAuthClients.stream()
                .filter(client -> client.provider() == provider)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원되지 않는 소셜 로그인 플랫폼입니다."));
    }

    private String resolveNickname(SocialUserInfo userInfo) {
        String raw = StringUtils.hasText(userInfo.nickname())
                ? userInfo.nickname()
                : userInfo.email().split("@")[0];    // 닉네임 제공 미동의 시 이메일 아이디로 대체
        return raw.length() > 30 ? raw.substring(0, 30) : raw;
    }
}