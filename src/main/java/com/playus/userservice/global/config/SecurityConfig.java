package com.playus.userservice.global.config;

import com.playus.userservice.domain.oauth.handler.CustomSuccessHandler;
import com.playus.userservice.domain.oauth.handler.CustomFailureHandler;
import com.playus.userservice.domain.oauth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.playus.userservice.domain.oauth.service.CustomOAuth2UserService;
import com.playus.userservice.global.jwt.JwtFilter;
import com.playus.userservice.global.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
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
		"/user/api", "/user/api/**",
		"/user/notifications", "/user/notifications/**",
		"/user/auth/reissue",
		"/user/auth/logout",
		"/community/api", "/community/api/**",
		"/twp/api", "/twp/api/**"
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
		"/user/auth/reissue",
		"/user/auth/logout",
		"/presigned-url",
		"/register",
		"/actuator/**"
	};

	@Bean
	public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
		return new HttpCookieOAuth2AuthorizationRequestRepository();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)

				// CORS 설정
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// 401, 403 예외 처리
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(EntryPoint())      // 인증 실패(401)
						.accessDeniedHandler((req, res, denied) -> { // 권한 실패(403)
							if (!res.isCommitted()) {
								res.setStatus(HttpStatus.FORBIDDEN.value());
								res.setContentType("application/json;charset=UTF-8");
								res.getWriter().write("{\"error\":\"Access Denied\"}");
							}
						})
				)

				// JWT 필터
				.addFilterBefore(
						new JwtFilter(jwtUtil, redisTemplate),
						UsernamePasswordAuthenticationFilter.class
				)

				// OAuth2 로그인 설정
				.oauth2Login(oauth2 -> oauth2
						.authorizationEndpoint(endpoint ->
								endpoint.authorizationRequestRepository(authorizationRequestRepository())
						)
						.userInfoEndpoint(u -> u.userService(customOAuth2UserService))
						.successHandler(customSuccessHandler)
						.failureHandler(customFailureHandler)
				)

				// 인증·인가 규칙
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(WHITELISTED_PATHS).permitAll()  // 화이트리스트
						.requestMatchers(INTERNAL_PATHS).permitAll()     // 내부 API
						.anyRequest().authenticated()
				)

				// 세션 없이 JWT 기반
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
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

	@Bean
	public AuthenticationEntryPoint EntryPoint() {
		return (req, res, ex) -> {

			res.setStatus(HttpStatus.UNAUTHORIZED.value());     // 401
			res.setContentType("application/json;charset=UTF-8");
			res.getWriter().write("{\"error\":\"Unauthorized\"}");
		};
	}

	@Bean @Order(1)
	public SecurityFilterChain internalChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher(INTERNAL_PATHS)
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		return http.build();
	}
}
