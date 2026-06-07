package com.devinterview.api.config;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 프로덕션(Azure) 기동 시 필수 시크릿·리다이렉트 URI가 기본값이 아닌지 검증한다.
 */
@Slf4j
@Component
@Profile("prod")
public class ProductionEnvironmentValidator {

    private static final String JWT_PLACEHOLDER = "replace-with-long-jwt-secret-at-least-32-bytes";
    private static final String GOOGLE_CLIENT_ID_PLACEHOLDER = "local-dev-google-client-id-placeholder";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth2.redirect-uri:}")
    private String oauth2RedirectUri;

    @Value("${app.swagger.enabled:true}")
    private boolean swaggerEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void validate() {
        List<String> errors = new ArrayList<>();

        if (jwtSecret == null || jwtSecret.isBlank() || JWT_PLACEHOLDER.equals(jwtSecret)) {
            errors.add("JWT_SECRET must be configured with a secure value");
        } else if (jwtSecret.length() < 32) {
            errors.add("JWT_SECRET must be at least 32 characters");
        }

        if (googleClientId == null || googleClientId.isBlank() || GOOGLE_CLIENT_ID_PLACEHOLDER.equals(googleClientId)) {
            errors.add("GOOGLE_CLIENT_ID must be configured");
        }

        if (oauth2RedirectUri == null || oauth2RedirectUri.isBlank()) {
            errors.add("OAUTH2_REDIRECT_URI must be configured");
        } else if (oauth2RedirectUri.contains("localhost")) {
            errors.add("OAUTH2_REDIRECT_URI must not point to localhost in production");
        }

        if (swaggerEnabled) {
            errors.add("Swagger must be disabled in production (SWAGGER_ENABLED=false)");
        }

        if (!errors.isEmpty()) {
            String message = "Production security validation failed: " + String.join("; ", errors);
            log.error(message);
            throw new IllegalStateException(message);
        }

        log.info("[Security] Production environment validation passed");
    }
}
