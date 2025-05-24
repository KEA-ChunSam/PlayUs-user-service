package com.playus.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playus.userservice.domain.notification.controller.NotificationController;
import com.playus.userservice.domain.notification.service.NotificationService;
import com.playus.userservice.domain.user.controller.*;
import com.playus.userservice.domain.user.service.FavoriteTeamService;
import com.playus.userservice.domain.user.service.ProfileSetupService;
import com.playus.userservice.domain.user.service.UserProfileReadService;
import com.playus.userservice.domain.user.service.UserService;
import com.playus.userservice.global.exception.ExceptionAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.playus.userservice.global.jwt.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;


@ActiveProfiles("test")
@WebMvcTest(controllers = {
        ProfileSetupController.class,
        FavoriteTeamController.class,
        UserController.class,
        UserProfileController.class,
        PartyUserController.class,
        NotificationController.class
})
@Import({ControllerTestSupport.TestSecurityConfig.class, ExceptionAdvice.class})
public abstract class ControllerTestSupport {


    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected FavoriteTeamService favoriteTeamService;

    @MockitoBean
    protected ProfileSetupService profileSetupService;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected UserProfileReadService userProfileReadService;

    @MockitoBean
    protected NotificationService notificationService;

    @MockitoBean
    protected JwtUtil jwtUtil;

    @MockitoBean
    protected RedisTemplate<String, String> redisTemplate;

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable);
            http.formLogin(AbstractHttpConfigurer::disable);
            http.httpBasic(AbstractHttpConfigurer::disable);
            http.authorizeHttpRequests(auth -> auth
                    .anyRequest().hasAuthority("USER")
            );

            // 세션 관리: Stateless
            http.sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

            return http.build();

        }
    }
}
