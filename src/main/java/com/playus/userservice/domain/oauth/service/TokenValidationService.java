package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenValidationService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlacklisted(String token) {
        String jti = jwtUtil.getJti(token);
        return redisTemplate.hasKey("blacklist:" + jti);
    }
}
