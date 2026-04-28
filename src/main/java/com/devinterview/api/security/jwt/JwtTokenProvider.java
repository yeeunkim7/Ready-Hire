package com.devinterview.api.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-expiration-seconds:3600}") long accessTokenExpirationSeconds,
        @Value("${jwt.refresh-token-expiration-seconds:1209600}") long refreshTokenExpirationSeconds
    ) {
        this.signingKey = buildSigningKey(secret);
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String createAccessToken(Long userId, String email, String role, String planType) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("userId", userId)
            .claim("email", email)
            .claim("role", role)
            .claim("planType", planType)
            .issuedAt(Date.from(now.toInstant()))
            .expiration(Date.from(expiresAt.toInstant()))
            .signWith(signingKey)
            .compact();
    }

    public String createRefreshToken(Long userId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = now.plusSeconds(refreshTokenExpirationSeconds);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("tokenType", "REFRESH")
            .issuedAt(Date.from(now.toInstant()))
            .expiration(Date.from(expiresAt.toInstant()))
            .signWith(signingKey)
            .compact();
    }

    public boolean validateToken(String token) {
        Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
        return true;
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(String token) {
        Object userIdClaim = getClaims(token).get("userId");
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(getClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getPlanType(String token) {
        return getClaims(token).get("planType", String.class);
    }

    public OffsetDateTime getExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        return expiration.toInstant().atOffset(ZoneOffset.UTC);
    }

    private SecretKey buildSigningKey(String secret) {
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length > 0) {
                return Keys.hmacShaKeyFor(decoded);
            }
        } catch (IllegalArgumentException ignored) {
            // fallback to plain secret bytes
        }

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
