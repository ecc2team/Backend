package com.zeropick.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 이미 가입된 이메일, 이메일 미인증, 인증코드 만료/쿨다운 등
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), e.getMessage()));
    }

    // 로그인 실패, 인증코드 불일치 등
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), e.getMessage()));
    }

    // @Valid 검증 실패 (필수값 누락, 형식 오류 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        FieldError error = e.getBindingResult().getFieldError();
        String message = (error != null)
                ? error.getField() + ": " + error.getDefaultMessage()
                : "요청 값이 올바르지 않습니다.";
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message));
    }

    // 요청 본문이 없거나 JSON 형식이 깨진 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), "요청 본문을 읽을 수 없습니다. 형식을 확인해주세요."));
    }

    // 필수 쿼리 파라미터 누락 (예: check-email?email= 빠뜨린 경우)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), e.getParameterName() + " 파라미터가 필요합니다."));
    }

    // 그 외 예상하지 못한 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("예상하지 못한 서버 오류", e);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), "서버 내부 오류가 발생했습니다."));
    }
}