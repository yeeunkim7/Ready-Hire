package com.devinterview.api.security.filter;

import com.devinterview.api.common.dto.ApiResponse;
import com.devinterview.api.common.exception.ErrorCode;
import com.devinterview.api.security.jwt.JwtTokenProvider;
import com.devinterview.api.security.user.CustomUserDetails;
import com.devinterview.api.security.user.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = bearerToken.substring(7);
        try {
            jwtTokenProvider.validateToken(token);
            Long userId = jwtTokenProvider.getUserId(token);
            CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            SecurityContextHolder.clearContext();
            writeError(response, ErrorCode.TOKEN_EXPIRED);
        } catch (SecurityException ex) {
            SecurityContextHolder.clearContext();
            writeError(response, ErrorCode.TOKEN_TAMPERED);
        } catch (MalformedJwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            writeError(response, ErrorCode.TOKEN_MALFORMED);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            writeError(response, ErrorCode.TOKEN_INVALID);
        }
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body = ApiResponse.failure("[" + errorCode.getCode() + "] " + errorCode.getDefaultMessage());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
