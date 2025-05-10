package com.playus.userservice.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtUtil {

    private final SecretKey key;
    public static final long ACCESS_EXPIRE_MS  = 6 * 60 * 60 * 1000;      // 6시간
    public static final long REFRESH_EXPIRE_MS = 7L * 24 * 60 * 60 * 1000; // 7일

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(byteSecretKey);
    }

    //엑세스토큰
    public String createAccessToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRE_MS))
                .signWith(key)
                .compact();
    }

    //리프레시토큰
    public String createRefreshToken(String userId, String role) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRE_MS))
                .signWith(key)
                .compact();
    }

    public Boolean isExpired(String token) {
        Date expiration = extractPayload(token).getExpiration();
        return expiration.before(new Date());
    }

    public String getUserId(String token) {
        return extractPayload(token).getSubject();
    }

    public String getRole(String token) {
        return extractPayload(token).get("role", String.class);
    }

    private Claims extractPayload(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .require("type", "access")
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
