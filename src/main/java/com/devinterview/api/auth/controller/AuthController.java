package com.devinterview.api.auth.controller;

import com.devinterview.api.auth.dto.AuthTokenResponse;
import com.devinterview.api.auth.dto.LogoutRequest;
import com.devinterview.api.auth.dto.RefreshRequest;
import com.devinterview.api.auth.dto.SignupRequest;
import com.devinterview.api.auth.dto.SignupResponse;
import com.devinterview.api.auth.service.AuthService;
import com.devinterview.api.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    /**
     * 로그인은 JsonUsernamePasswordAuthenticationFilter가 처리합니다. (POST /api/auth/login)
     */

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthTokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed.", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }
}
