package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.oauth.enums.TokenType;
import com.playus.userservice.domain.user.dto.UserDto;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import com.playus.userservice.global.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Value("${cookie.sameSite}")
    private String cookieSameSite;

    @Value("${cookie.domain}")
    private String domain;

    public void verifyAndRenewToken(HttpServletRequest req, HttpServletResponse res) {

        String refresh = tokenService.resolveToken(req, TokenType.REFRESH);
        boolean refreshExpired = isExpired(refresh);

        if (refresh == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MISSING_REFRESH_TOKEN");
        }

        if (refreshExpired) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED");
        }

        String newAccess = tokenService.reissueAccessToken(req, res);
        authenticate(newAccess);
    }


    public void logout(HttpServletRequest req, HttpServletResponse res) {
        // Refresh Token 삭제
        tokenService.deleteRefreshToken(req, res);

        // Access Token 블랙리스트
        String access = tokenService.resolveToken(req, TokenType.ACCESS);
        if (access != null && !jwtUtil.isExpired(access)) {
            String jti = jwtUtil.getJti(access);

            long ttlSeconds = jwtUtil.getRemainingExpirationTime(access) / 1000;

            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set("blacklist:" + jti, "", Duration.ofSeconds(ttlSeconds));
            }
        }
        // 클라이언트 쿠키(Access) 만료 처리
        ResponseCookie expiredAccess = ResponseCookie.from("Access", "")
                .secure(cookieSecure)   // 운영환경에선 true 로 변경
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .domain(domain)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, expiredAccess.toString());

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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MISSING_TOKEN");
        }
    
        //블랙리스트 체크
        String jti = jwtUtil.getJti(token);
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + jti))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "BLACKLISTED_TOKEN");
        }


        String type = jwtUtil.getType(token);
        if (!"access".equals(type)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_TYPE");
        }

        // 엑세스 토큰 클레임 검사
        String userId = jwtUtil.getUserId(token);
        String role = jwtUtil.getRole(token);
        int age = jwtUtil.getAge(token);
        String gender = jwtUtil.getGender(token);

        CustomOAuth2User principal =
                new CustomOAuth2User(
                        UserDto.fromJwt(Long.parseLong(userId), Role.valueOf(role), age, Gender.valueOf(gender))
                );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
