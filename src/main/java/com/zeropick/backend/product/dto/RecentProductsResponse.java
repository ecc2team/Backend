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
            List<String> dietaryTags,
            String riskLevel,
            OffsetDateTime viewedAt
    ) {}
}