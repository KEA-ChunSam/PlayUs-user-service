package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserDto;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.global.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_COOKIE = "Refresh";
    private static final String REDIS_PREFIX   = "refresh:";
    private static final String BEARER_PREFIX  = "Bearer ";

    /**
     * case1: 둘 다 만료 → 401
     * case2: access 만료 + refresh 유효 → access만 자동 재발급
     * case3: access 유효 + refresh 만료 → refresh만 자동 재발급
     * case4: 둘 다 유효 → 정상 인증 세팅
     */

    public void validateAndRefreshTokens(HttpServletRequest req, HttpServletResponse res) {
        String access  = extractAccessToken(req);
        String refresh = extractRefreshToken(req);

        // 토큰이 아예 없는 경우 (로그인 필요)
        if (access == null && refresh == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }

        boolean accessExpired;
        boolean refreshExpired;

        try {
            accessExpired = (access == null) || jwtUtil.isExpired(access);
        } catch (JwtException | IllegalArgumentException e) {
            accessExpired = true;
        }

        try {
            refreshExpired = (refresh == null) || jwtUtil.isExpired(refresh);
        } catch (JwtException | IllegalArgumentException e) {
            refreshExpired = true;
        }

        // case1: 둘 다 만료
        if (accessExpired && refreshExpired) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Both tokens expired");
        }

        // case2: access 만료, refresh 유효 → access 재발급
        if (accessExpired && !refreshExpired) {
            // Redis에서 리프레시 토큰 유효성 검증
            String userId = jwtUtil.getUserId(refresh);
            String stored = redisTemplate.opsForValue().get(REDIS_PREFIX + userId);
            if (!refresh.equals(stored)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
            }

            reissueAccessToken(req, res);
            authenticateWithRefreshToken(refresh);
            return;
        }

        // case3: access 유효, refresh 만료 → refresh 재발급
        if (!accessExpired && refreshExpired) {
            reissueRefreshToken(access, res);
            authenticateWithAccessToken(access);
            return;
        }

        // case4: 둘 다 유효 → 정상 인증 세팅
        authenticateWithAccessToken(access);
    }

    /** /reissue - refresh로 access만 재발급 */
    public void reissueAccessToken(HttpServletRequest req, HttpServletResponse res) {
        String refresh = extractRefreshToken(req);
        boolean expired;
        try {
            expired = jwtUtil.isExpired(refresh);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
        if (expired) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        // Redis 검증 한 번 더
        String userId = jwtUtil.getUserId(refresh);
        String stored = redisTemplate.opsForValue().get(REDIS_PREFIX + userId);
        if (!refresh.equals(stored)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }

        String role = jwtUtil.getRole(refresh);
        String newAccessToken = jwtUtil.createAccessToken(userId, role);

        res.setHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken);
        res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);
    }

    /** case3 - access 유효 → 새로운 refresh 발급 */
    private void reissueRefreshToken(String access, HttpServletResponse res) {
        if (jwtUtil.isExpired(access)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
        String userId = jwtUtil.getUserId(access);
        String role = jwtUtil.getRole(access);

        String newRefresh = jwtUtil.createRefreshToken(userId, role);
        redisTemplate.opsForValue()
                .set(REDIS_PREFIX + userId,
                        newRefresh,
                        JwtUtil.REFRESH_EXPIRE_MS,
                        TimeUnit.MILLISECONDS);

        Cookie cookie = new Cookie(REFRESH_COOKIE, newRefresh);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);    // 프로덕션에서는 true
        cookie.setPath("/");
        cookie.setMaxAge((int) (JwtUtil.REFRESH_EXPIRE_MS / 1000));
        res.addCookie(cookie);
    }

    /** case2,4 공통 - Access 토큰으로 인증 설정 */
    private void authenticateWithAccessToken(String access) {
        String userId = jwtUtil.getUserId(access);
        String role = jwtUtil.getRole(access);
        setAuthentication(userId, role);
    }

    /** case2 -Refresh 토큰으로 인증 설정 */
    private void authenticateWithRefreshToken(String refresh) {
        String userId = jwtUtil.getUserId(refresh);
        String role = jwtUtil.getRole(refresh);
        setAuthentication(userId, role);
    }

    private void setAuthentication(String userId, String role) {
        CustomOAuth2User principal = new CustomOAuth2User(
                UserDto.fromJwt(Long.parseLong(userId), Role.valueOf(role))
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /** 개선된 logout 로직 */
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        try {
            String refresh = extractRefreshToken(req);
            if (refresh != null) {
                try {
                    String userId = jwtUtil.getUserId(refresh);
                    redisTemplate.delete(REDIS_PREFIX + userId);
                } catch (JwtException | IllegalArgumentException e) {
                    log.warn("Invalid refresh token during logout: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.info("No refresh cookie found during logout: {}", e.getMessage());
        }

        Cookie expired = new Cookie(REFRESH_COOKIE, null);
        expired.setHttpOnly(true);
        expired.setSecure(true);
        expired.setPath("/");
        expired.setAttribute("SameSite", "Strict");
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
            return null;
        }
        for (Cookie c : req.getCookies()) {
            if (REFRESH_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}