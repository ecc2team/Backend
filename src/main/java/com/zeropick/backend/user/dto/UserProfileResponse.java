package com.zeropick.backend.user.dto;

import com.zeropick.backend.user.AuthProvider;

import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        AuthProvider provider,
        List<String> preferredCategories,      // 선호하는 제품 카테고리 (Category.code)
        List<String> dislikedIngredients,      // 피하고 싶은 성분 (Ingredient.code)
        List<String> allergyFlags              // 알레르기 유발 성분 (Ingredient.code)
) {
}