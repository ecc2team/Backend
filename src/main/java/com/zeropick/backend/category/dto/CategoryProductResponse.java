package com.zeropick.backend.category.dto;

import java.util.List;

public record CategoryProductResponse(
        Long productId,
        String productName,
        Integer grade,
        Boolean warningAdditive,
        List<String> keyIngredients
) {
}
