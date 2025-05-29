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
        String userId = jwtUtil.getUserId(refresh);
        String role   = jwtUtil.getRole(refresh);
        User user = userRepository.findByIdAndActivatedTrue(Long.parseLong(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        LocalDateTime bdt = user.getBirth().atStartOfDay();
        int age = AgeUtils.calculateAgeGroup(bdt);
        String gender = user.getGender().name();

        // 새 Access 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(userId, role, age, gender);

        // Access 토큰을 HttpOnly 쿠키로 설정
        ResponseCookie accessCookie = ResponseCookie.from("Access", newAccessToken)
                .secure(false)  // 운영환경(HTTPS)에서는 항상 true
                .path("/")
                .maxAge(Duration.ofMillis(JwtUtil.ACCESS_EXPIRE_MS))
                .sameSite("Lax")
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
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
                .secure(false)  // 운영환경(HTTPS)에서는 항상 true
                .sameSite("Lax")
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
                    .secure(false)  // 운영환경(HTTPS)에서는 항상 true
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ZERO)
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
