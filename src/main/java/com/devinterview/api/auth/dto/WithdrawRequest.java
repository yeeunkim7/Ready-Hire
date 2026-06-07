package com.devinterview.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(
    @NotBlank String refreshToken
) {
}
