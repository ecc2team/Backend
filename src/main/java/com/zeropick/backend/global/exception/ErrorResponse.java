package com.zeropick.backend.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String errorCode,
        String message
) {
    // 기존 코드와의 호환성을 위한 2개 파라미터 생성자 (errorCode는 null)
    public ErrorResponse(int status, String message) {
        this(status, null, message);
    }
}