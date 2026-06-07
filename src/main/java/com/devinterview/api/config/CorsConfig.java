package com.devinterview.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
public class CorsConfig {

    private static final List<String> DEFAULT_ORIGIN_PATTERNS = List.of(
        "http://localhost:*",
        "https://ready-hire-vert.vercel.app",
        "https://*.vercel.app"
    );

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://ready-hire-vert.vercel.app}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(resolveOriginPatterns());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> resolveOriginPatterns() {
        Set<String> patterns = new LinkedHashSet<>(DEFAULT_ORIGIN_PATTERNS);
        for (String origin : parseOrigins(allowedOrigins)) {
            patterns.add(toOriginPattern(origin));
        }
        List<String> resolved = new ArrayList<>(patterns);
        log.info("[CORS] Allowed origin patterns: {}", resolved);
        return resolved;
    }

    private String toOriginPattern(String origin) {
        if (origin.startsWith("http://localhost")) {
            return "http://localhost:*";
        }
        return origin;
    }

    private List<String> parseOrigins(String origins) {
        return Arrays.stream(origins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList();
    }
}
