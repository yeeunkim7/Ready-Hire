package com.devinterview.api.config;

import com.devinterview.api.auth.filter.JsonUsernamePasswordAuthenticationFilter;
import com.devinterview.api.auth.handler.JwtLoginFailureHandler;
import com.devinterview.api.auth.handler.JwtLoginSuccessHandler;
import com.devinterview.api.auth.oauth2.CookieOAuth2AuthorizationRequestRepository;
import com.devinterview.api.auth.oauth2.CustomOAuth2UserService;
import com.devinterview.api.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.devinterview.api.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.security.filter.JwtAuthenticationFilter;
import com.devinterview.api.security.user.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final CookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository;
    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;
    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final JwtLoginFailureHandler jwtLoginFailureHandler;

    @Value("${app.swagger.enabled:true}")
    private boolean swaggerEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        AuthenticationManager authenticationManager
    ) throws Exception {
        JsonUsernamePasswordAuthenticationFilter loginFilter =
            new JsonUsernamePasswordAuthenticationFilter(objectMapper);
        loginFilter.setAuthenticationManager(authenticationManager);
        loginFilter.setAuthenticationSuccessHandler(jwtLoginSuccessHandler);
        loginFilter.setAuthenticationFailureHandler(jwtLoginFailureHandler);
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                if (request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<Void> body = ApiResponse.failure(
                        "[" + ErrorCode.AUTH_ERROR.getCode() + "] " + ErrorCode.AUTH_ERROR.getDefaultMessage()
                    );
                    response.getWriter().write(objectMapper.writeValueAsString(body));
                    return;
                }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }))
            .authorizeHttpRequests(auth -> {
                List<String> publicPaths = new ArrayList<>(List.of(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/health",
                    "/api/health/**",
                    "/api/payments/webhook",
                    "/login/oauth2/**",
                    "/oauth2/**"
                ));
                if (swaggerEnabled) {
                    publicPaths.add("/swagger-ui/**");
                    publicPaths.add("/v3/api-docs/**");
                }
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(publicPaths.toArray(String[]::new)).permitAll()
                    .anyRequest().authenticated();
            })
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(auth -> auth
                    .authorizationRequestRepository(cookieOAuth2AuthorizationRequestRepository)
                )
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            .authenticationProvider(daoAuthenticationProvider())
            .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, JsonUsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
