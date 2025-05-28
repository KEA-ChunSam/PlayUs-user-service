package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlacklisted(String token) {

        if (token == null || token.trim().isEmpty()) {
            log.warn("토큰이 null이거나 비어있습니다");
            throw new IllegalArgumentException("유효하지 않은 토큰입니다");
        }

        try {
            String jti = jwtUtil.getJti(token);

            if (jti == null) {
                log.warn("JWT에서 jti를 추출할 수 없습니다");
                throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다");
            }

            return redisTemplate.hasKey("blacklist:" + jti);

        } catch (Exception e) {
            log.error("토큰 블랙리스트 확인 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
}
