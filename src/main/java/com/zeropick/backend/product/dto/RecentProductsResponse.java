package com.zeropick.backend.product.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record RecentProductsResponse(
        Integer count,
        List<RecentProductItem> items
) {
    public record RecentProductItem(
            Long productId,
            String productName,
            List<String> images,
            Integer score,           // riskLevel 제거 후 score 반영
            OffsetDateTime viewedAt  // OffsetDateTime 반영
    ) {}
}