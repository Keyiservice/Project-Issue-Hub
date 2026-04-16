package com.company.opl.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        byte[] keyBytes = jwtProperties.getSecret().length() >= 32
                ? jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
                : Decoders.BASE64.decode("b3BsLXBsYXRmb3JtLXN1cGVyLXNlY3JldC1rZXktZm9yLWp3dA==");
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId,
                                String username,
                                String realName,
                                String departmentCode,
                                String departmentName,
                                List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(jwtProperties.getExpireMinutes() * 60);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("realName", realName)
                .claim("departmentCode", departmentCode)
                .claim("departmentName", departmentName)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public String generateMfaToken(Long userId, String username, long expireSeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expireSeconds);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("mfaPending", true)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
