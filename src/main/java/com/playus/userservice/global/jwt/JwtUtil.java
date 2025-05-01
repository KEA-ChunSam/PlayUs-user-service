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

    /** JWT 생성 (userId, role 기반) */
    public String createJwt(String userId, String role, Long expiredMs) {
        return Jwts.builder()
                .setSubject(userId)  // 🟡 userId를 subject로 저장
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key)
                .compact();
    }

    /** JWT 생성 - category 포함 버전 (optional) */
    public String createJwt(String category, String userId, String role, Long expiredMs) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("category", category)
                .claim("role", role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key)
                .compact();
    }

    /** JWT 만료 여부 확인 */
    public Boolean isExpired(String token) {
        Date expiration = extractPayload(token).getExpiration();
        return expiration.before(new Date());
    }

    /** userId 추출 */
    public String getUserId(String token) {
        return extractPayload(token).getSubject(); // 🟡 subject에 저장된 userId 꺼냄
    }

    /** 역할(role) 추출 */
    public String getRole(String token) {
        return extractPayload(token).get("role", String.class);
    }

    /** category 추출 (optional) */
    public String getCategory(String token) {
        return extractPayload(token).get("category", String.class);
    }

    /** 내부 Payload 추출 */
    private Claims extractPayload(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

