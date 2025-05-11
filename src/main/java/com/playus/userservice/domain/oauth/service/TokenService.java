package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.oauth.enums.TokenType;
import com.playus.userservice.global.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_COOKIE  = "Refresh";
    private static final String REDIS_PREFIX    = "refresh:";
    private static final String BEARER_PREFIX   = "Bearer ";

    public String resolveToken(HttpServletRequest req, TokenType type) {
        if (type == TokenType.ACCESS) {
            return extractAccessToken(req);
        } else {
            return extractRefreshToken(req);
        }
    }
    /**
     * TokenService 는 “토큰 발급·재발급(+리프레시 토큰 파싱)”만 담당
     * reissue - refresh로 access만 재발급
    */
    public void reissueAccessToken(HttpServletRequest req, HttpServletResponse res) {
        String refresh = extractRefreshToken(req);
        // 만료/변조 체크
        boolean expired;
        try {
            expired = jwtUtil.isExpired(refresh);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
        if (expired) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_만료된 토큰입니다");
        }
        // Redis 검증
        String userId;
        String role;
        try {
            userId = jwtUtil.getUserId(refresh);
            role   = jwtUtil.getRole(refresh);
            } catch (JwtException | IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_IN_REDIS");
        }
        // 새 Access 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(userId, role);
        res.setHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken);
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);
    }

    /** Access 유효할 경우 Refresh 재발급 */
    public void reissueRefreshToken(String access, HttpServletResponse res) {

        if (access == null || jwtUtil.isExpired(access)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        String userId = jwtUtil.getUserId(access);
        String role   = jwtUtil.getRole(access);
        String newRefresh = jwtUtil.createRefreshToken(userId, role);

        redisTemplate.opsForValue()
                .set(REDIS_PREFIX + userId,
                        newRefresh,
                        JwtUtil.REFRESH_EXPIRE_MS,
                        TimeUnit.MILLISECONDS);

        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, newRefresh)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(JwtUtil.REFRESH_EXPIRE_MS))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteRefreshToken(HttpServletRequest req, HttpServletResponse res) {
        String refresh;
        try {
            refresh = resolveToken(req, TokenType.REFRESH);
            String userId = jwtUtil.getUserId(refresh);
            redisTemplate.delete(REDIS_PREFIX + userId);

        } catch (ResponseStatusException | JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        } finally {
            ResponseCookie expired = ResponseCookie.from(REFRESH_COOKIE, "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(Duration.ZERO)
                    .build();
            res.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
        }
    }

    private String extractAccessToken(HttpServletRequest req) {
        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (h != null && h.startsWith(BEARER_PREFIX)) {
            return h.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest req) {
        if (req.getCookies() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
        for (Cookie c : req.getCookies()) {
            if (REFRESH_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
    }
}
