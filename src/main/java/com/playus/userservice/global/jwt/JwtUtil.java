package com.playus.userservice.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;


@Component
public class JwtUtil {

    private final SecretKey key;
    public static final long ACCESS_EXPIRE_MS  = 6 * 60 * 60 * 1000;      // 6시간
    public static final long REFRESH_EXPIRE_MS = 7L * 24 * 60 * 60 * 1000; // 7일

    public JwtUtil(@Value("${spring.jwt.secret}") String secret) {
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(byteSecretKey);
    }

    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    //엑세스토큰
    public String createAccessToken(String userId, String role, int age, String gender) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .claim("role", role)
                .claim("age", age)
                .claim("gender", gender)
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
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getJti(String token) {
        return extractPayload(token).getId();
    }

    public String getType(String token) {
        return extractPayload(token).get("type", String.class);
    }

    public long getRemainingExpirationTime(String token) {
        return extractPayload(token).getExpiration().getTime() - System.currentTimeMillis();
    }
}
