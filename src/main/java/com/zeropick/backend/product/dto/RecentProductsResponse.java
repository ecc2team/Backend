package com.zeropick.backend.product.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record RecentProductsResponse(
        Integer totalElements,
        List<RecentProductItem> content
) {
    public record RecentProductItem(
            Long productId,
            String productName,
            String imageUrl, // 이미지 URL 필드 추가
            List<String> dietaryTags,
            String riskLevel,
            OffsetDateTime viewedAt
    ) {}
}