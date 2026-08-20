package com.zeropick.backend.category.dto;

import com.zeropick.backend.category.Category;

public record CategoryResponse(
        Long id,
        String name,
        String code
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode()
        );
    }
}
