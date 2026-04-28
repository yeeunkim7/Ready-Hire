package com.devinterview.api.auth.service;

import com.devinterview.api.auth.dto.AuthTokenResponse;
import com.devinterview.api.auth.dto.LoginRequest;
import com.devinterview.api.auth.entity.UserRefreshToken;
import com.devinterview.api.auth.exception.AuthException;
import com.devinterview.api.auth.repository.UserRefreshTokenRepository;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.enums.Provider;
import com.devinterview.api.domain.repository.UserRepository;
import com.devinterview.api.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndProvider(request.email(), Provider.LOCAL)
            .orElseThrow(() -> new AuthException("Invalid email or password."));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password.");
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

        if (saved.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new AuthException("Refresh token expired.");
        }

        User user = saved.getUser();

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
        userRefreshTokenRepository.findByRefreshToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
        });
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
