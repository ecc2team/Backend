package com.zeropick.backend.user;

import com.zeropick.backend.category.Category;
import com.zeropick.backend.category.CategoryRepository;
import com.zeropick.backend.email.EmailVerificationService;
import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import com.zeropick.backend.user.dto.*;
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

import java.time.OffsetDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private static final int NICKNAME_MAX_LENGTH = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService  emailVerificationService;
    private final UserAllergyRepository userAllergyRepository;
    private final UserPreferredIngredientRepository userPreferredIngredientRepository;
    private final UserPreferredCategoryRepository userPreferredCategoryRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;

    public EmailCheckResponse checkEmail(String email){
        boolean isAvailable = !userRepository.existsByEmailAndDeletedAtIsNull(email);
        return new EmailCheckResponse(email, isAvailable);
    }

    // 닉네임 중복 확인
    public NicknameCheckResponse checkNickname(String nickname) {
        String trimmed = nickname == null ? "" : nickname.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
        if (trimmed.length() > NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("닉네임은 " + NICKNAME_MAX_LENGTH + "자 이하여야 합니다.");
        }

        // 탈퇴 회원의 닉네임은 다시 사용 가능
        boolean isAvailable = !userRepository.existsByNicknameAndDeletedAtIsNull(trimmed);
        return new NicknameCheckResponse(trimmed, isAvailable);
    }

    public FindAccountResponse findAccount(String email) {
        User user= userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        return new FindAccountResponse(user.getId(), user.getEmail(), user.getProvider());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, String emailVerifySessionId) {
        emailVerificationService.assertSessionVerified(emailVerifySessionId, request.email());

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new IllegalStateException("소셜 로그인 계정은 비밀번호를 재설정할 수 없습니다."));

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        emailVerificationService.invalidate(request.email());
    }

    @Transactional
    public void withdraw(Long userId) {
        // 이미 탈퇴 처리된 경우 아무 것도 하지 않고 조용히 종료
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .ifPresent(User::withdraw);
    }

    public UserProfileResponse getMyProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProvider(),
                user.getGender(),
                user.getBirthDate(),
                user.getHeight(),
                user.getWeight(),
                user.getActivityLevel(),
                userPreferredCategoryRepository.findAllByUserWithCategory(user).stream()
                        .map(upc -> upc.getCategory().getCode())
                        .toList(),
                userPreferredIngredientRepository.findAllByUserWithIngredient(user).stream()
                        .map(upi -> upi.getIngredient().getCode())
                        .toList(),
                userAllergyRepository.findAllByUserWithIngredient(user).stream()
                        .map(ua -> ua.getIngredient().getCode())
                        .toList()
        );
    }

    // 마이페이지 취향 수정: 기존 설정을 전부 지우고 새로 들어온 값으로 통째로 갈아끼운다.
    @Transactional
    public UserPreferencesResponse updatePreferences(User user, UserPreferencesRequest request) {

        User realUser = userRepository.findByIdAndDeletedAtIsNull(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 유저 기본 프로필(신체 정보) 업데이트
        if (request.profile() != null) {
            realUser.updateProfile(
                    request.profile().gender(),
                    request.profile().birthDate(),
                    request.profile().height(),
                    request.profile().weight(),
                    request.profile().activityLevel()
            );
        }

        // 2. 기존 취향/알레르기 설정 삭제
        userPreferredCategoryRepository.deleteAllByUser(realUser);
        userPreferredIngredientRepository.deleteAllByUser(realUser);
        userAllergyRepository.deleteAllByUser(realUser);

        // 3. 새로운 설정 저장 로직
        List<Category> categories = categoryRepository.findAllByCodeIn(request.preferredCategories());
        categories.forEach(category -> userPreferredCategoryRepository.save(
                UserPreferredCategory.builder().user(realUser).category(category).build()
        ));

        List<Ingredient> disliked = ingredientRepository.findAllByCodeIn(request.dislikedIngredients());
        disliked.forEach(ingredient -> userPreferredIngredientRepository.save(
                UserPreferredIngredient.builder().user(realUser).ingredient(ingredient).build()
        ));

        List<Ingredient> allergies = ingredientRepository.findAllByCodeIn(request.allergyFlags());
        allergies.forEach(ingredient -> userAllergyRepository.save(
                UserAllergy.builder().user(realUser).ingredient(ingredient).build()
        ));

        return new UserPreferencesResponse(realUser.getId(), OffsetDateTime.now());
    }
}
