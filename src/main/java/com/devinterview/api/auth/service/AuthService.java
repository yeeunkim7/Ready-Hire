package com.devinterview.api.auth.service;

import com.devinterview.api.auth.dto.AuthTokenResponse;
import com.devinterview.api.auth.dto.SignupRequest;
import com.devinterview.api.auth.dto.SignupResponse;
import com.devinterview.api.auth.entity.UserRefreshToken;
import com.devinterview.api.auth.exception.AuthException;
import com.devinterview.api.auth.repository.UserRefreshTokenRepository;
import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.AccountStatus;
import com.devinterview.api.domain.enums.PlanType;
import com.devinterview.api.domain.enums.Provider;
import com.devinterview.api.domain.enums.Role;
import com.devinterview.api.domain.repository.UserRepository;
import com.devinterview.api.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-token-expiration-seconds:3600}")
    private long accessTokenExpirationSeconds;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();

        if (!request.password().equals(request.passwordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (userRepository.existsByEmailAndProviderAndDeletedAtIsNull(email, Provider.LOCAL)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = userRepository.save(
            User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .planType(PlanType.FREE)
                .build()
        );

        log.info("[Auth] LOCAL signup completed: userId={}, email={}", user.getId(), email);

        return new SignupResponse(user.getId(), user.getEmail(), user.getPlanType().name());
    }

    @Transactional
    public AuthTokenResponse issueTokens(User user) {
        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.ACCOUNT_WITHDRAWN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getPlanType().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        upsertRefreshToken(user, refreshToken);

        return new AuthTokenResponse(accessToken, refreshToken, "Bearer", accessTokenExpirationSeconds);
    }

    @Transactional
    public AuthTokenResponse refresh(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken);
        Claims claims = jwtTokenProvider.getClaims(refreshToken);

        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new AuthException("Invalid token type.");
        }

        UserRefreshToken saved = userRefreshTokenRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new AuthException("Refresh token not found."));

        if (Boolean.TRUE.equals(saved.getRevoked())) {
            throw new AuthException("Refresh token revoked.");
        }

        if (saved.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new AuthException("Refresh token expired.");
        }

        User user = saved.getUser();
        if (user.isDeleted()) {
            saved.setRevoked(true);
            throw new CustomException(ErrorCode.ACCOUNT_WITHDRAWN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getPlanType().name()
        );
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        saved.setRefreshToken(newRefreshToken);
        saved.setExpiresAt(jwtTokenProvider.getExpiration(newRefreshToken));
        saved.setRevoked(false);

        return new AuthTokenResponse(newAccessToken, newRefreshToken, "Bearer", accessTokenExpirationSeconds);
    }

    @Transactional
    public void logout(String refreshToken) {
        userRefreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(token -> token.setRevoked(true));
    }

    private void upsertRefreshToken(User user, String refreshToken) {
        UserRefreshToken tokenEntity = userRefreshTokenRepository.findByUserId(user.getId())
            .orElseGet(() -> UserRefreshToken.builder().user(user).build());

        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setExpiresAt(jwtTokenProvider.getExpiration(refreshToken));
        tokenEntity.setRevoked(false);

        userRefreshTokenRepository.save(tokenEntity);
    }
}
