package com.zeropick.backend.ingredient.service;

import com.zeropick.backend.ingredient.dto.IngredientDetailResponse;
import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    // 성분 상세 정보 조회
    public IngredientDetailResponse getIngredientDetail(String code) {
        Ingredient ingredient = ingredientRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 성분 코드입니다. code=" + code));

        return new IngredientDetailResponse(
                ingredient.getCode(),
                ingredient.getName(),
                ingredient.getType(),
                ingredient.getRiskLevel(),
                ingredient.getSummary(),
                ingredient.getDescription()
        );
    }
}