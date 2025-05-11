package com.playus.userservice.domain.config;

import com.playus.userservice.domain.oauth.handler.CustomSuccessHandler;
import com.playus.userservice.domain.oauth.service.CustomOAuth2UserService;
import com.playus.userservice.global.jwt.JwtFilter;
import com.playus.userservice.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000"
    );

    private static final String[] WHITELISTED_PATHS = {
            "/oauth2/authorization/kakao",
            "/login/oauth2/code/kakao",
            "/oauth2/authorization/naver",
            "/login/oauth2/code/naver",
            "/api/v1/auth/reissue",
            "/api/v1/auth/logout"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(
                        new JwtFilter(jwtUtil, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class
                )

                .cors(cors -> cors.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest req) {
                        CorsConfiguration c = new CorsConfiguration();
                        c.setAllowedOrigins(ALLOWED_ORIGINS);
                        c.setAllowedMethods(Collections.singletonList("*"));
                        c.setAllowedHeaders(Collections.singletonList("*"));
                        c.setAllowCredentials(true);
                        c.setExposedHeaders(Collections.singletonList("Authorization"));
                        return c;
                    }
                }))

                // OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                )

                // 인증/인가
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELISTED_PATHS)
                        .permitAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
