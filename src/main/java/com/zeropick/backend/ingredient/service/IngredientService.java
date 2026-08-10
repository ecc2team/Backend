package com.zeropick.backend.ingredient.service;

import com.zeropick.backend.ingredient.dto.IngredientDetailResponse;
import com.zeropick.backend.ingredient.dto.IngredientResponse;
import com.zeropick.backend.ingredient.entity.Ingredient;
import com.zeropick.backend.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    // 1. 성분 마스터 데이터 전체 목록 조회
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
                .map(i -> new IngredientResponse(
                        i.getId(),
                        i.getCode(),
                        i.getName(),
                        i.getType(),
                        i.getRiskLevel(),
                        i.getSummary()
                ))
                .collect(Collectors.toList());
    }

    // 2. 기존 성분 상세 정보 조회
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