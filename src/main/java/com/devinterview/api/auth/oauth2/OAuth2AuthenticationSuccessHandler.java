package com.devinterview.api.auth.oauth2;

import com.devinterview.api.auth.entity.UserRefreshToken;
import com.devinterview.api.auth.repository.UserRefreshTokenRepository;
import com.devinterview.api.common.exception.CustomException;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.domain.entity.User;
import com.devinterview.api.domain.repository.UserRepository;
import com.devinterview.api.security.jwt.JwtTokenProvider;
import com.devinterview.api.security.user.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2 로그인 성공 시 JWT를 발급하고 프론트엔드로 리다이렉트하는 핸들러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRepository userRepository;
    private final OAuth2FrontendUrls oAuth2FrontendUrls;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException, ServletException {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            log.error("OAuth2 principal type mismatch: {}", principal == null ? "null" : principal.getClass().getName());
            getRedirectStrategy().sendRedirect(request, response, oAuth2FrontendUrls.loginUrl("oauth2_failed"));
            return;
        }

        User user = userRepository.findById(userDetails.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.AUTH_ERROR, "User not found for OAuth2 token save."));
        if (user.isDeleted()) {
            log.warn("OAuth2 login blocked for withdrawn account: userId={}", userDetails.getUserId());
            getRedirectStrategy().sendRedirect(request, response, oAuth2FrontendUrls.loginUrl("account_withdrawn"));
            return;
        }

        String accessToken = jwtTokenProvider.createAccessToken(
            userDetails.getUserId(),
            userDetails.getEmail(),
            userDetails.getRole(),
            userDetails.getPlanType()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUserId());

        upsertRefreshToken(user, userDetails, refreshToken);

        String targetUrl = UriComponentsBuilder.fromUriString(oAuth2FrontendUrls.callbackUrl())
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken)
            .build()
            .toUriString();

        log.info("OAuth2 JWT issued for userId={}", userDetails.getUserId());
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void upsertRefreshToken(User user, CustomUserDetails userDetails, String refreshToken) {
        UserRefreshToken tokenEntity = userRefreshTokenRepository.findByUserId(userDetails.getUserId())
            .orElseGet(UserRefreshToken::new);

        tokenEntity.setUser(user);
        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setExpiresAt(jwtTokenProvider.getExpiration(refreshToken));
        tokenEntity.setRevoked(false);

        userRefreshTokenRepository.save(tokenEntity);
    }
}
