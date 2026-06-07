package com.devinterview.api.common.exception;

import com.devinterview.api.common.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        log.warn("[API] CustomException: code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.failure(formatMessage(errorCode, ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String detail = fieldError == null
            ? ErrorCode.VALIDATION_ERROR.getDefaultMessage()
            : fieldError.getField() + ": " + fieldError.getDefaultMessage();

        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("[API] DataIntegrityViolationException", ex);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "데이터 저장 제약 조건 오류가 발생했습니다.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException ex) {
        log.error("[API] DataAccessException", ex);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, "데이터 저장 중 오류가 발생했습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception ex) {
        log.error("[API] Unhandled exception", ex);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(ApiResponse.failure(formatMessage(errorCode, message)));
    }

    private String formatMessage(ErrorCode errorCode, String message) {
        return "[" + errorCode.getCode() + "] " + message;
    }
}
