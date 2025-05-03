package com.playus.userservice.domain.oauth.controller;

import com.playus.userservice.global.jwt.JwtUtil;
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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class TokenController {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_COOKIE_NAME       = "Refresh";
    private static final String REDIS_REFRESH_KEY_PREFIX  = "refresh:";

    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request,
                                        HttpServletResponse response) {
        // 1) HttpOnly 쿠키에서 Refresh Token 꺼내기 (없으면 예외 발생)
        String refreshToken = extractRefreshFromCookie(request);

        // 2) 만료 여부 체크
        if (jwtUtil.isExpired(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token이 만료되었습니다.");
        }

        // 3) Redis 에 저장된 것과 일치하는지 확인
        String userId = jwtUtil.getUserId(refreshToken);
        String stored = redisTemplate.opsForValue()
                .get(REDIS_REFRESH_KEY_PREFIX + userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token이 일치하지 않습니다.");
        }

        // 4) 새 Access Token 발급
        String role            = jwtUtil.getRole(refreshToken);
        String newAccessToken  = jwtUtil.createAccessToken(userId, role);

        // 5) 응답 헤더에 Access Token 담아 내려주기
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {

        // 1) 기존 쿠키에서 Refresh 토큰 꺼내기
        String refreshToken = extractRefreshFromCookie(request);

        // 2) Redis에서 삭제
        String userId = jwtUtil.getUserId(refreshToken);
        redisTemplate.delete(REDIS_REFRESH_KEY_PREFIX + userId);

        // 3) 쿠키 만료
        Cookie expiredCookie = new Cookie(REFRESH_COOKIE_NAME, null);
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(true);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);

        return ResponseEntity.noContent().build();
    }

    private String extractRefreshFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "HttpOnly 쿠키에 Refresh Token이 없습니다.");
    }
}
