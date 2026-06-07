package com.devinterview.api.auth.handler;

import com.devinterview.api.auth.dto.AuthTokenResponse;
import com.devinterview.api.auth.service.AuthService;
import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.repository.UserRepository;
import com.devinterview.api.security.user.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));

        AuthTokenResponse tokenResponse = authService.issueTokens(user);
        ApiResponse<AuthTokenResponse> body = ApiResponse.success("Login successful.", tokenResponse);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
