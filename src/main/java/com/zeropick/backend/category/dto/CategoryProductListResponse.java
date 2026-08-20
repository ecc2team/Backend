package com.zeropick.backend.category.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CategoryProductListResponse(
        List<CategoryProductResponse> content,
        PageInfo pageInfo
) {
    public record PageInfo(
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            @JsonProperty("isLast") boolean isLast
    ) {
    }
}