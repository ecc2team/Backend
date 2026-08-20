package com.zeropick.backend.category.dto;

public record CategoryBestProductResponse(
        int rank,
        Long productId,
        String productName,
        Integer score,
        Boolean warningAdditive,
        Integer viewCount
) {
}