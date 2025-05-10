package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.oauth.enums.TokenType;
import com.playus.userservice.global.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * case1: 둘 다 만료 → 401
     * case2: access 만료 + refresh 유효 → access 재발급
     * case3: access 유효 + refresh 만료 → refresh 재발급
     * case4: 둘 다 유효 → 정상 인증 세팅
     */
    public void verifyAndRenewToken(HttpServletRequest req, HttpServletResponse res) {
        String access  = tokenService.resolveToken(req, TokenType.ACCESS);
        String refresh = tokenService.resolveToken(req, TokenType.REFRESH);

        boolean accessExpired  = isExpired(access);
        boolean refreshExpired = isExpired(refresh);

        // case1
        if (accessExpired && refreshExpired) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Both tokens expired");
        }
        // case2
        if (accessExpired) {
            tokenService.reissueAccessToken(req, res);
            authenticate(refresh);
            return;
        }
        // case3
        if (refreshExpired) {
            tokenService.reissueRefreshToken(access, res);
            authenticate(access);
            return;
        }
        // case4
        authenticate(access);
    }

    public void logout(HttpServletRequest req, HttpServletResponse res) {
        // Refresh Token 삭제 (쿠키 만료 & Redis key 삭제)
        tokenService.deleteRefreshToken(req, res);

        // Access Token 블랙리스트
        String access = tokenService.resolveToken(req, TokenType.ACCESS);
        if (access != null && !jwtUtil.isExpired(access)) {
            String jti = jwtUtil.getJti(access);

            // 남은 만료 시간(초) 계산
            long ttlSeconds = jwtUtil.getRemainingExpirationTime(access) / 1000;

            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set("blacklist:" + jti, "", Duration.ofSeconds(ttlSeconds));
            }
        }
    }

    private boolean isExpired(String token) {
        if (token == null) return true;
        try {
            return jwtUtil.isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    private void authenticate(String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        String userId = jwtUtil.getUserId(token);
        String role   = jwtUtil.getRole(token);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                new com.playus.userservice.domain.oauth.dto.CustomOAuth2User(
                        com.playus.userservice.domain.user.dto.UserDto.fromJwt(
                                Long.parseLong(userId),
                                com.playus.userservice.domain.user.enums.Role.valueOf(role)
                        )
                ),
                null,
                new com.playus.userservice.domain.oauth.dto.CustomOAuth2User(
                        com.playus.userservice.domain.user.dto.UserDto.fromJwt(
                                Long.parseLong(userId),
                                com.playus.userservice.domain.user.enums.Role.valueOf(role)
                        )
                ).getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
