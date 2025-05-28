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

        try {
            String jti = jwtUtil.getJti(token);

            if (jti == null) {
                log.warn("JWT에서 jti를 추출할 수 없습니다");
                throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다");
            }
            return redisTemplate.hasKey("blacklist:" + jti);

        } catch (JwtException jwtEx) {
            log.error("JWT 파싱 오류: {}", jwtEx.getMessage());
            throw new IllegalArgumentException("유효하지 않은 토큰입니다");

        } catch (RedisConnectionFailureException redisEx) {
            log.error("Redis 연결 실패: {}", redisEx.getMessage());
            throw new IllegalStateException("토큰 블랙리스트 확인 서비스에 문제가 발생했습니다");
        }
    }
}
