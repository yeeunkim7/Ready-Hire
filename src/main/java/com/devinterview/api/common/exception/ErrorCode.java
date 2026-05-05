package com.devinterview.api.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    AUTH_ERROR(HttpStatus.UNAUTHORIZED, "AUTH_001", "Authentication failed."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "JWT access token expired."),
    TOKEN_TAMPERED(HttpStatus.UNAUTHORIZED, "AUTH_003", "JWT signature validation failed."),
    TOKEN_MALFORMED(HttpStatus.BAD_REQUEST, "AUTH_004", "Invalid JWT format."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_005", "JWT validation failed."),
    OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AUTH_006", "지원하지 않는 OAuth2 제공자입니다."),
    OAUTH2_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH_007", "OAuth2 계정에서 이메일을 가져올 수 없습니다."),
    AI_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "AI_001", "AI service call failed."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_001", "Invalid request."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "Unexpected server error.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
