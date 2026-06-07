package com.devinterview.api.auth.dto;

public record SignupResponse(
    Long userId,
    String email,
    String planType
) {
}
