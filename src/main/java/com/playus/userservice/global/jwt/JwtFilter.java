package com.playus.userservice.global.jwt;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserDto;
import com.playus.userservice.domain.user.enums.Gender;
import com.playus.userservice.domain.user.enums.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 내부 호출 /user/api/ 은 필터에서 제외
        return request.getRequestURI().startsWith("/user/api/");
    }

    public JwtFilter(JwtUtil jwtUtil,
        RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Access".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            try {
                String jti = jwtUtil.getJti(token);
                if (redisTemplate.hasKey("blacklist:" + jti)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "BLACKLISTED_TOKEN");
                }

                if (!jwtUtil.isExpired(token)) {
                    String userId = jwtUtil.getUserId(token);
                    String role = jwtUtil.getRole(token);
                    int age = jwtUtil.getAge(token);
                    String gender = jwtUtil.getGender(token);

                    CustomOAuth2User principal = new CustomOAuth2User(
                            UserDto.fromJwt(Long.parseLong(userId), Role.valueOf(role), age, Gender.valueOf(gender))
                    );
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("토큰 인증 실패: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
