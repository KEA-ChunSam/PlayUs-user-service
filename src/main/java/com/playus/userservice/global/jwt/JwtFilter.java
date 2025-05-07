package com.playus.userservice.global.jwt;

import com.playus.userservice.domain.oauth.dto.CustomOAuth2User;
import com.playus.userservice.domain.user.dto.UserDto;
import com.playus.userservice.domain.user.enums.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

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

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // Access Token이 유효하면 SecurityContext 설정
                if (!jwtUtil.isExpired(token)) {
                    String userId = jwtUtil.getUserId(token);
                    String role   = jwtUtil.getRole(token);

                    CustomOAuth2User principal = new CustomOAuth2User(
                            UserDto.fromJwt(Long.parseLong(userId), Role.valueOf(role))
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
