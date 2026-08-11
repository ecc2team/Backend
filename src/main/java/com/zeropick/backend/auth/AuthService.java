package com.zeropick.backend.auth;

import com.zeropick.backend.auth.dto.*;
import com.zeropick.backend.category.CategoryRepository;
import com.zeropick.backend.email.EmailVerificationService;
import com.zeropick.backend.global.security.JwtUtil;
import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import com.zeropick.backend.category.Category;
import com.zeropick.backend.category.CategoryRepository;
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

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (!emailVerificationService.isEmailVerified(request.email())) {
            throw new IllegalStateException("이메일 인증이 필요합니다.");
        }
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

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        user.updateRefreshToken(refreshToken);

        return new TokenResponse(user.getId(), accessToken, refreshToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtUtil.isValid(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));
        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        user.updateRefreshToken(null);
    }
}