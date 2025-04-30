package com.playus.userservice.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(byteSecretKey);
    }

    public String getUsername(String token) {
        return extractPayload(token)
                .get("username", String.class);
    }

    public String getRole(String token) {
        return extractPayload(token)
                .get("role", String.class);
    }

    public String getCategory(String token) {
        return extractPayload(token)
                .get("category", String.class);
    }

    public Boolean isExpired(String token) {
        Date expiration = extractPayload(token)
                .getExpiration();

        return expiration.before(new Date());
    }

    public String createJwt(String username, String role, Long expiredMs) {
        Map<String, Object> claims = Map.of(
                "username", username,
                "role", role
        );

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key)  // ✅ 0.12.3에서는 signWith(key)만 사용 (알고리즘 자동 선택)
                .compact();
    }

    public String createJwt(String category, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key)
                .compact();
    }

    private Claims extractPayload(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

