package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.oauth.enums.TokenType;
import com.playus.userservice.domain.user.entity.User;
import com.playus.userservice.domain.user.repository.write.UserRepository;
import com.playus.userservice.global.jwt.JwtUtil;
import com.playus.userservice.global.util.AgeUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    private static final String ACCESS_COOKIE  = "Access";
    private static final String REFRESH_COOKIE  = "Refresh";
    private static final String REDIS_PREFIX    = "refresh:";

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Value("${cookie.sameSite}")
    private String cookieSameSite;

    @Value("${cookie.domain}")
    private String domain;

    /**
     * TokenService 는 “토큰 발급,재발급만 담당
     * reissue - refresh로 access만 재발급
    */
    public String reissueAccessToken(HttpServletRequest req, HttpServletResponse res) {

        String refresh = extractRefreshToken(req);

        // 만료,변조 체크
        if (jwtUtil.isExpired(refresh)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "REFRESH_TOKEN_EXPIRED");
        }

        // Redis 검증
        String userId = jwtUtil.getUserId(refresh);
        String redisKey = REDIS_PREFIX + userId;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_SESSION");
        }

        // 유저 검증
        String role = jwtUtil.getRole(refresh);
        User user = userRepository.findByIdAndActivatedTrue(Long.parseLong(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        LocalDateTime bdt = user.getBirth().atStartOfDay();
        int age = AgeUtils.calculateAgeGroup(bdt);
        String gender = user.getGender().name();

        // 새 Access 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(userId, role, age, gender);

        ResponseCookie accessCookie = ResponseCookie.from("Access", newAccessToken)
                .secure(cookieSecure)  // 운영환경(HTTPS)에서는 항상 true
                .path("/")
                .maxAge(Duration.ofMillis(JwtUtil.ACCESS_EXPIRE_MS))
                .sameSite(cookieSameSite)
                .domain(domain)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        // 리프레쉬 토큰 유효기간 하루 남으면 새로 발급
        long remaining = jwtUtil.getRemainingExpirationTime(refresh);
        long oneDayMs = 24L * 60 * 60 * 1000;

        if (remaining < oneDayMs) {
            String rotated = jwtUtil.createRefreshToken(userId, role);
            redisTemplate.opsForValue()
                    .set(redisKey, rotated, JwtUtil.REFRESH_EXPIRE_MS, TimeUnit.MILLISECONDS);

            ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE, rotated)
                    .secure(cookieSecure).sameSite(cookieSameSite).domain(domain)
                    .path("/").httpOnly(true)
                    .maxAge(Duration.ofMillis(JwtUtil.REFRESH_EXPIRE_MS))
                    .build();
            res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        return newAccessToken;
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
                    .secure(cookieSecure)  // 운영환경(HTTPS)에서는 항상 true
                    .sameSite(cookieSameSite)
                    .path("/")
                    .maxAge(Duration.ZERO)
                    .domain(domain)
                    .build();
            res.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
        }
    }

    public String resolveToken(HttpServletRequest req, TokenType type) {
        return type == TokenType.ACCESS
                ? extractAccessToken(req)
                : extractRefreshToken(req);
    }

    private String extractAccessToken(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (ACCESS_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        }
        for (Cookie c : cookies) {
            if (REFRESH_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
    }
}
