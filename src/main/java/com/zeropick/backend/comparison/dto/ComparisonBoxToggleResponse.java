package com.zeropick.backend.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ComparisonBoxToggleResponse {
    private Long productId;
    private Boolean isInComparisonBox;
}