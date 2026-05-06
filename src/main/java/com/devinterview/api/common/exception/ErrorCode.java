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
    DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "INTERVIEW_001", "오늘 무료 면접 횟수(3회)를 모두 사용했습니다."),
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW_002", "면접 정보를 찾을 수 없습니다."),
    QUESTION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "INTERVIEW_003", "질문 생성에 실패했습니다."),
    INTERVIEW_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "INTERVIEW_004", "이미 완료된 면접입니다."),
    INTERVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "INTERVIEW_005", "해당 면접에 접근 권한이 없습니다."),
    ANSWER_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "INTERVIEW_006", "답변 분석에 실패했습니다."),
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
