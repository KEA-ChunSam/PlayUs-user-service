package com.playus.userservice.domain.oauth.handler;


import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        try {
            CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
            String userId = customUser.getUserDto().getId().toString();

            // 일반 유저_관리자 구별
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String role = authorities.iterator().next().getAuthority();

            //Access/Refresh 토큰 생성
            String accessToken  = jwtUtil.createAccessToken(userId, role);
            String refreshToken = jwtUtil.createRefreshToken(userId);

            //Redis에 Refresh Token 저장 (key = refresh:{userId})
            redisTemplate.opsForValue()
                    .set("refresh:" + userId,
                            refreshToken,
                            JwtUtil.REFRESH_EXPIRE_MS,
                            TimeUnit.MILLISECONDS);


            //쿠키로 토큰 전송
            Cookie accessCookie = createCookie("Authorization", accessToken, (int)(JwtUtil.ACCESS_EXPIRE_MS / 1000));
            Cookie refreshCookie = createCookie("Refresh", refreshToken, (int)(JwtUtil.REFRESH_EXPIRE_MS / 1000));
            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);

            //프론트엔드로 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000/choice-team");

        } catch (Exception e) {
            log.error("로그인 성공 후 토큰 발급/리디렉션 중 예외 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Authentication Success Handling Error");
        }
    }

    private Cookie createCookie(String name, String value, int maxAgeSec) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSec);
        // cookie.setSecure(true); // HTTPS에서 필요시 활성화
        return cookie;
    }
}
