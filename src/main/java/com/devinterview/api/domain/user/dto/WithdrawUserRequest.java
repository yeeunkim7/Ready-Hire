package com.devinterview.api.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawUserRequest(
    @NotBlank String refreshToken
) {
}
