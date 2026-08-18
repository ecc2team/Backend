package com.zeropick.backend.user.dto;

import java.util.List;

// 마이페이지 취향/알레르기 수정 요청
public record UserPreferencesRequest(
        List<String> preferredCategories,
        List<String> dislikedIngredients,
        List<String> allergyFlags
) {
    public List<String> preferredCategories() {
        return preferredCategories == null ? List.of() : preferredCategories;
    }
    public List<String> dislikedIngredients() {
        return dislikedIngredients == null ? List.of() : dislikedIngredients;
    }
    public List<String> allergyFlags() {
        return allergyFlags == null ? List.of() : allergyFlags;
    }
}