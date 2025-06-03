package com.playus.userservice.global.config;

import com.playus.userservice.domain.oauth.handler.CustomSuccessHandler;
import com.playus.userservice.domain.oauth.handler.CustomFailureHandler;
import com.playus.userservice.domain.oauth.service.CustomOAuth2UserService;
import com.playus.userservice.global.jwt.JwtFilter;
import com.playus.userservice.global.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomSuccessHandler customSuccessHandler;
	private final CustomFailureHandler customFailureHandler;
	private final JwtUtil jwtUtil;
	private final RedisTemplate<String, String> redisTemplate;

	private static final List<String> ALLOWED_ORIGINS = List.of(
			"http://localhost:3000",
			"https://web.playus.o-r.kr"
	);

	private static final String[] INTERNAL_PATHS = {
			"/user/api/**",
	};

	private static final String[] WHITELISTED_PATHS = {
			"/error",
			"/swagger-ui/**",
			"/v3/api-docs/**",
			"/user/api/**",
			"/community/api/**",
			"/oauth2/authorization/kakao",
			"/login/oauth2/code/kakao",
			"/oauth2/authorization/naver",
			"/login/oauth2/code/naver",
			"/api/v1/auth/reissue",
			"/api/v1/auth/logout",
			"/actuator/**"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)

				.exceptionHandling(ex -> ex.authenticationEntryPoint(EntryPoint()))

				// JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
				.addFilterBefore(
						new JwtFilter(jwtUtil, redisTemplate),
						UsernamePasswordAuthenticationFilter.class
				)

				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// OAuth2
				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(u -> u.userService(customOAuth2UserService))
						.successHandler(customSuccessHandler)
						.failureHandler(customFailureHandler)
				)

				// 인증/인가
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(WHITELISTED_PATHS)
						.permitAll()
						.requestMatchers(INTERNAL_PATHS)
						.permitAll()
						.anyRequest().authenticated()
				)

				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}


	private AuthenticationEntryPoint EntryPoint() {
		return (HttpServletRequest request, HttpServletResponse response,
				org.springframework.security.core.AuthenticationException authException) -> {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType("application/json;charset=UTF-8");

			String origin = request.getHeader("Origin");
			if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
				response.setHeader("Access-Control-Allow-Origin", origin);
				response.setHeader("Access-Control-Allow-Credentials", "true");
			}

			response.getWriter().write("{\"error\":\"Unauthorized\"}");
		};
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(ALLOWED_ORIGINS);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		config.setExposedHeaders(List.of("Authorization"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}