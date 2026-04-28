package com.devinterview.api.auth.controller;

import com.devinterview.api.auth.dto.ApiMessageResponse;
import com.devinterview.api.auth.dto.AuthTokenResponse;
import com.devinterview.api.auth.dto.LoginRequest;
import com.devinterview.api.auth.dto.LogoutRequest;
import com.devinterview.api.auth.dto.RefreshRequest;
import com.devinterview.api.auth.exception.AuthException;
import com.devinterview.api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiMessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(new ApiMessageResponse("Logged out successfully."));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiMessageResponse> handleAuthException(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiMessageResponse(ex.getMessage()));
    }
}
