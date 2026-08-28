package com.zeropick.backend.comparison.exception;

public class ComparisonCategoryMismatchException extends RuntimeException {
    public ComparisonCategoryMismatchException() {
        super("같은 카테고리의 상품만 비교할 수 있습니다.");
    }
}