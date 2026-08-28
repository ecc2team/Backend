package com.zeropick.backend.auth.dto;

import com.zeropick.backend.user.dto.ProfileRequest;

import java.util.List;

public record OnboardingRequest(
        ProfileRequest profile,
        List<String> preferredCategories,
        List<String> dislikedIngredients,
        List<String> allergyFlags
) {
    // null로 들어와도 NPE 안 나게 방어
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
