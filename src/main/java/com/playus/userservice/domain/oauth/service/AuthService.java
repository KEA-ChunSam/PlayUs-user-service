package com.playus.userservice.domain.oauth.service;

import com.playus.userservice.domain.oauth.enums.TokenType;
import com.playus.userservice.global.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    /**
     * case1: 둘 다 만료 → 401
     * case2: access 만료 + refresh 유효 → access 재발급
     * case3: access 유효 + refresh 만료 → refresh 재발급
     * case4: 둘 다 유효 → 정상 인증 세팅
     */
    public void verifyAndRenewTokens(HttpServletRequest req, HttpServletResponse res) {
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
        tokenService.deleteRefreshToken(req, res);
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
