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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
        // Redis 검증
        String userId = jwtUtil.getUserId(refresh);
        String stored = redisTemplate.opsForValue().get(REDIS_PREFIX + userId);
        if (!refresh.equals(stored)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
        // 새 Access 토큰 발급
        String role           = jwtUtil.getRole(refresh);
        String newAccessToken = jwtUtil.createAccessToken(userId, role);
        res.setHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken);
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);
    }

    /** Access 유효할 경우 Refresh 재발급 */
    public void reissueRefreshToken(String access, HttpServletResponse res) {

        if (access == null || jwtUtil.isExpired(access)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "INVALID_TOKEN"
            );
        }

        String userId = jwtUtil.getUserId(access);
        String role   = jwtUtil.getRole(access);
        String newRefresh = jwtUtil.createRefreshToken(userId, role);

        redisTemplate.opsForValue()
                .set(REDIS_PREFIX + userId,
                        newRefresh,
                        JwtUtil.REFRESH_EXPIRE_MS,
                        TimeUnit.MILLISECONDS);

        Cookie cookie = new Cookie(REFRESH_COOKIE, newRefresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int)(JwtUtil.REFRESH_EXPIRE_MS / 1000));
        res.addCookie(cookie);
    }

    public void deleteRefreshToken(HttpServletRequest req, HttpServletResponse res) {
        String refresh;
        try {
            refresh = resolveToken(req, TokenType.REFRESH);
        } catch (ResponseStatusException e) {
            refresh = null;
        }
        if (refresh != null) {
            String userId = jwtUtil.getUserId(refresh);
            redisTemplate.delete(REDIS_PREFIX + userId);
        }
        Cookie expired = new Cookie(REFRESH_COOKIE, null);
        expired.setHttpOnly(true);
        expired.setSecure(true);
        expired.setPath("/");
        expired.setMaxAge(0);
        res.addCookie(expired);
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
