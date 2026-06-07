package com.devinterview.api.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth2 로그인 실패 시 프론트엔드 에러 페이지로 리다이렉트하는 핸들러.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2FrontendUrls oAuth2FrontendUrls;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
        throws IOException, ServletException {
        String errorCode = OAuth2AuthErrors.resolveLoginErrorCode(exception);
        log.warn("OAuth2 login failed: errorCode={}, message={}", errorCode, exception.getMessage());
        getRedirectStrategy().sendRedirect(request, response, oAuth2FrontendUrls.loginUrl(errorCode));
    }
}
