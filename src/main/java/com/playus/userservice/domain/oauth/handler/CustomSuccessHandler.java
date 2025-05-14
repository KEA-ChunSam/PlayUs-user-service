package com.playus.userservice.domain.oauth.handler;


import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.frontend.success-redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        try {
            CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
            String userId = customUser.getUserDto().getId().toString();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities == null || authorities.isEmpty()) {
                log.error("인증 객체에 권한이 없습니다. userId={}", userId);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "권한 정보 누락");
                return;
            }
            String role = authorities.iterator().next().getAuthority();

            // Access/Refresh 토큰 생성
            String accessToken  = jwtUtil.createAccessToken(userId, role);
            String refreshToken = jwtUtil.createRefreshToken(userId, role);

            // Redis에 Refresh Token 저장 (key = refresh:{userId})
            redisTemplate.opsForValue()
                    .set("refresh:" + userId,
                            refreshToken,
                            JwtUtil.REFRESH_EXPIRE_MS,
                            TimeUnit.MILLISECONDS);


            // Refresh Token -  HttpOnly·Secure쿠키
            Cookie refreshCookie = new Cookie("Refresh", refreshToken);
            refreshCookie.setHttpOnly(true);
            //refreshCookie.setSecure(true);         // HTTPS 환경에서는 반드시 true
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge((int)(JwtUtil.REFRESH_EXPIRE_MS / 1000));
            response.addCookie(refreshCookie);

            // Access Token -  Authorization 헤더
            response.setHeader("Authorization", "Bearer " + accessToken);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");

            // 로그인 후 프론트 페이지로 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, redirectUri);

        } catch (Exception e) {
            log.error("로그인 성공 후 토큰 발급/리디렉션 중 예외 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Authentication Success Handling Error");
        }
    }
}
