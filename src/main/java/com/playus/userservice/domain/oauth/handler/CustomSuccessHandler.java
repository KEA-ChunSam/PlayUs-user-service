package com.playus.userservice.domain.oauth.handler;


import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.repository.write.FavoriteTeamRepository;
import com.playus.userservice.global.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final FavoriteTeamRepository favoriteTeamRepository;

    @Value("${app.frontend.success-redirect-uri}")   // 프로필 미설정(=선호팀 없음)
    private String redirectNeedSetup;

    @Value("${app.frontend.success-redirect-uri2}")  // 선호팀 ≥ 1개
    private String redirectHome;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        try {
            CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
            String userId = customUser.getUserDto().getId().toString();
            String role   = authentication.getAuthorities().iterator().next().getAuthority();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities == null || authorities.isEmpty()) {
                log.error("인증 객체에 권한이 없습니다. userId={}", userId);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "권한 정보 누락");
                return;
            }

            // Access/Refresh 토큰 생성
            String accessToken  = jwtUtil.createAccessToken(userId, role);
            String refreshToken = jwtUtil.createRefreshToken(userId, role);

            // Redis에 Refresh Token 저장 (key = refresh:{userId})
            redisTemplate.opsForValue()
                    .set("refresh:" + userId,
                            refreshToken,
                            JwtUtil.REFRESH_EXPIRE_MS,
                            TimeUnit.MILLISECONDS);


            ResponseCookie refreshCookie = ResponseCookie.from("Refresh", refreshToken)
                    .httpOnly(true)
                    .secure(false)                           // 운영환경(HTTPS)에서는 항상 true
                    .path("/")
                    .maxAge(Duration.ofMillis(JwtUtil.REFRESH_EXPIRE_MS))
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // 4) Access Token 쿠키 (HttpOnly, Secure, SameSite)
            ResponseCookie accessCookie = ResponseCookie.from("Access", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(Duration.ofMillis(JwtUtil.ACCESS_EXPIRE_MS))
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

            boolean hasFavorite = favoriteTeamRepository.existsByUserId(Long.parseLong(userId));
            String targetUri    = hasFavorite ? redirectHome : redirectNeedSetup;

            getRedirectStrategy().sendRedirect(request, response, targetUri);

        } catch (Exception e) {
            log.error("로그인 성공 후 토큰 발급/리디렉션 중 예외 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Authentication Success Handling Error");
        }
    }
}
