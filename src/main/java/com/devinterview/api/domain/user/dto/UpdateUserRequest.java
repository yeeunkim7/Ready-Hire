package com.devinterview.api.domain.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.") String password
) {
}
