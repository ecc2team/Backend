package com.zeropick.backend.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IngredientDetailResponse {

    private String code;
    private String name;
    private String ingredientType;
    private String riskLevel;
    private String summary;
    private String description;
}