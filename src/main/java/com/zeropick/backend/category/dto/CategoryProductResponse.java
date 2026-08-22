package com.zeropick.backend.category.dto;

import java.util.List;

public record CategoryProductResponse(
        Long productId,
        String productName,
        Integer score,
        Integer calories,
        Double sugar,
        Boolean warningAdditive,
        Integer viewCount,
        Integer compareCount,
        List<String> keyIngredients
) {
}