package com.devinterview.api.auth.handler;

import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        ErrorCode errorCode = exception instanceof DisabledException
            ? ErrorCode.ACCOUNT_WITHDRAWN
            : ErrorCode.AUTH_ERROR;
        String message = exception instanceof DisabledException
            ? errorCode.getDefaultMessage()
            : "이메일 또는 비밀번호가 올바르지 않습니다.";

        ApiResponse<Void> body = ApiResponse.failure("[" + errorCode.getCode() + "] " + message);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
