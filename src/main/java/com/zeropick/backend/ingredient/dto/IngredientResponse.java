package com.zeropick.backend.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IngredientResponse {
    private Long id;
    private String code;
    private String name;
    private String ingredientType; // DB의 type 컬럼 -> JSON의 ingredientType
    private String riskLevel;
    private String summary;
}