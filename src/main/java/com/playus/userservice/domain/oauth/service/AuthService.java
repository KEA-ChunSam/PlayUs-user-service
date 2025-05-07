package com.playus.userservice.domain.oauth.service;

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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_COOKIE = "Refresh";
    private static final String REDIS_PREFIX   = "refresh:";

    /**reissue*/
    public void reissueAccessToken(HttpServletRequest req, HttpServletResponse res) {
        String refresh = extractRefreshFromCookie(req);

        // 토큰 만료/변조 체크
        try {
            if (jwtUtil.isExpired(refresh)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        // Redis 저장 값과 일치 확인
        String userId = jwtUtil.getUserId(refresh);
        String stored = redisTemplate.opsForValue().get(REDIS_PREFIX + userId);
        if (stored == null || !stored.equals(refresh)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        // 새 Access Token 발급
        String role = jwtUtil.getRole(refresh);
        String newAccessToken = jwtUtil.createAccessToken(userId, role);

        // 헤더에 내려줌
        res.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);
    }

    /**logout*/
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        String refresh = extractRefreshFromCookie(req);
        // 삭제
        String userId = jwtUtil.getUserId(refresh);
        redisTemplate.delete(REDIS_PREFIX + userId);
        // 쿠키 만료
        Cookie expired = new Cookie(REFRESH_COOKIE, null);
        expired.setHttpOnly(true);
        expired.setSecure(true);
        expired.setPath("/");
        expired.setMaxAge(0);
        res.addCookie(expired);
    }

    private String extractRefreshFromCookie(HttpServletRequest req) {
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
