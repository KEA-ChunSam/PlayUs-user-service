package com.playus.userservice.domain.oauth.controller;

import com.playus.userservice.global.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_COOKIE_NAME      = "Refresh";
    private static final String REDIS_REFRESH_KEY_PREFIX = "refresh:";

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request,
                                     HttpServletResponse response) {
        // 쿠키에서 Refresh Token 꺼내기
        String refreshToken = extractRefreshFromCookie(request);

        // 토큰 유효성 및 만료 확인
        boolean expired;
        try {
            expired = jwtUtil.isExpired(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "유효하지 않은 Refresh Token입니다.");
        }
        if (expired) {
            return errorResponse(HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Refresh Token이 만료되었습니다.");
        }

        // Redis 에 저장된 토큰과 일치 확인
        String userId = jwtUtil.getUserId(refreshToken);
        String stored = redisTemplate.opsForValue()
                .get(REDIS_REFRESH_KEY_PREFIX + userId);
        if (stored == null || !stored.equals(refreshToken)) {
            return errorResponse(HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Refresh Token이 일치하지 않습니다.");
        }

        // 새 Access Token 발급
        String role           = jwtUtil.getRole(refreshToken);
        String newAccessToken = jwtUtil.createAccessToken(userId, role);

        // 응답 헤더에 담아 내려주기
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                HttpHeaders.AUTHORIZATION);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        // 쿠키에서 Refresh Token 꺼내기
        String refreshToken = extractRefreshFromCookie(request);
        if (refreshToken == null) {
            return errorResponse(HttpStatus.BAD_REQUEST,
                    "INVALID_REQUEST",
                    "Refresh Token이 쿠키에 존재하지 않습니다.");
        }

        // Redis에서 삭제
        String userId = jwtUtil.getUserId(refreshToken);
        redisTemplate.delete(REDIS_REFRESH_KEY_PREFIX + userId);

        // 쿠키 만료
        Cookie expired = new Cookie(REFRESH_COOKIE_NAME, null);
        expired.setHttpOnly(true);
        expired.setSecure(true);
        expired.setPath("/");
        expired.setMaxAge(0);
        response.addCookie(expired);

        return ResponseEntity.noContent().build();
    }

    private String extractRefreshFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }

        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
    }

    private ResponseEntity<Map<String,String>> errorResponse(HttpStatus status, String error, String description) {
        Map<String,String> body = Map.of(
                "error", error,
                "error_description", description
        );
        return ResponseEntity
                .status(status)
                .body(body);
    }
}
