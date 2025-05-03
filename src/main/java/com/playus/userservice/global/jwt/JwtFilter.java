package com.playus.userservice.global.jwt;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserDto;
import com.playus.userservice.domain.user.enums.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
case1: 둘 다 만료 → 401
case2: access 만료 + refresh 유효 → access만 자동 재발급
case3: access 유효 + refresh 만료 → refresh만 자동 재발급
case4: 둘 다 유효 → 정상 처리
 */


public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_COOKIE = "Refresh";
    private static final String REDIS_PREFIX   = "refresh:";

    public JwtFilter(JwtUtil jwtUtil, RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String access = extractAccessToken(req);
        String refresh = extractRefreshToken(req);

        boolean accessExpired  = access == null  || jwtUtil.isExpired(access);
        boolean refreshExpired = refresh == null || jwtUtil.isExpired(refresh);

        // case1: 둘 다 만료 → 401
        if (accessExpired && refreshExpired) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Both tokens expired");
            return;
        }

        // case2: access 만료, refresh 유효 → access 재발급
        if (accessExpired && !refreshExpired) {
            if (validateRefresh(refresh)) {
                String userId = jwtUtil.getUserId(refresh);
                String role   = jwtUtil.getRole(refresh);
                String newAccess = jwtUtil.createAccessToken(userId, role);

                // 새 Access를 헤더에 담아서 내려줌
                res.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess);
                res.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);

                setAuthentication(userId, role);
                chain.doFilter(req, res);
            } else {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
            }
            return;
        }

        // case3: access 유효, refresh 만료 → refresh 재발급
        if (!accessExpired && refreshExpired) {
            String userId = jwtUtil.getUserId(access);
            String role   = jwtUtil.getRole(access);
            String newRefresh = jwtUtil.createRefreshToken(userId, role);

            // Redis에 저장
            redisTemplate.opsForValue()
                    .set(REDIS_PREFIX + userId,
                            newRefresh,
                            JwtUtil.REFRESH_EXPIRE_MS,
                            TimeUnit.MILLISECONDS);

            // HttpOnly·Secure 쿠키로 내려줌
            Cookie cookie = new Cookie(REFRESH_COOKIE, newRefresh);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);    // 프로덕션에서는 반드시 true
            cookie.setPath("/");
            cookie.setMaxAge((int)(JwtUtil.REFRESH_EXPIRE_MS / 1000));
            res.addCookie(cookie);

            setAuthentication(userId, role);
            chain.doFilter(req, res);
            return;
        }

        // case4: 둘 다 유효 → 정상 처리
        if (!accessExpired && !refreshExpired) {
            String userId = jwtUtil.getUserId(access);
            String role   = jwtUtil.getRole(access);
            setAuthentication(userId, role);
            chain.doFilter(req, res);
        }
    }

    private String extractAccessToken(HttpServletRequest req) {
        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (h != null && h.startsWith("Bearer ")) {
            return h.substring(7);
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> REFRESH_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private boolean validateRefresh(String token) {
        try {
            String userId = jwtUtil.getUserId(token);
            String stored = redisTemplate.opsForValue().get(REDIS_PREFIX + userId);
            return token.equals(stored);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
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
}
