package com.playus.userservice.global.config.swagger;

import java.util.List;

import io.swagger.v3.oas.models.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;

@OpenAPIDefinition(info = @Info(
		title = "PlayUs User Service API",
		description = "PlayUs 사용자 관리 서비스 API 문서",
		version = "v1.0.0"))

@Configuration
public class SwaggerConfig {

	@Value("${spring.security.oauth2.client.registration.kakao.client-id}")
	private String kakaoClientId;

	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String kakaoClientSecret;

	@Value("${spring.security.oauth2.client.registration.naver.client-id}")
	private String naverClientId;

	@Value("${spring.security.oauth2.client.registration.naver.client-secret}")
	private String naverClientSecret;

	@Bean
	public OpenAPI openApi() {
		// Cookie 기반 인증 (Access 토큰)
		String cookieAuth = "AccessCookie";
		SecurityRequirement cookieRequirement = new SecurityRequirement().addList(cookieAuth);
		SecurityScheme cookieScheme = new SecurityScheme()
				.name("Access")
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.COOKIE);

		// OAuth2 Kakao 설정
		String oauth2Kakao = "OAuth2_Kakao";
		SecurityRequirement kakaoRequirement = new SecurityRequirement().addList(oauth2Kakao);
		SecurityScheme kakaoScheme = new SecurityScheme()
				.name(oauth2Kakao)
				.type(SecurityScheme.Type.OAUTH2)
				.flows(new OAuthFlows()
						.authorizationCode(new OAuthFlow()
								.authorizationUrl("http://localhost:8080/oauth2/authorization/kakao")
								.tokenUrl("http://localhost:8080/login/oauth2/code/kakao")
								.scopes(new Scopes()
										.addString("client_id", kakaoClientId)
										.addString("client_secret", kakaoClientSecret))));

		// OAuth2 Naver 설정
		String oauth2Naver = "OAuth2_Naver";
		SecurityRequirement naverRequirement = new SecurityRequirement().addList(oauth2Naver);
		SecurityScheme naverScheme = new SecurityScheme()
				.name(oauth2Naver)
				.type(SecurityScheme.Type.OAUTH2)
				.flows(new OAuthFlows()
						.authorizationCode(new OAuthFlow()
								.authorizationUrl("http://localhost:8080/oauth2/authorization/naver")
								.tokenUrl("http://localhost:8080/login/oauth2/code/naver")
								.scopes(new Scopes()
										.addString("client_id", naverClientId)
										.addString("client_secret", naverClientSecret))));

		// 서버 기본 URL
		Server server = new Server();
		server.setUrl("/");

		return new OpenAPI()
				.servers(List.of(server))

				// 보안 요구사항 순서대로 등록
				.addSecurityItem(cookieRequirement)
				.addSecurityItem(kakaoRequirement)
				.addSecurityItem(naverRequirement)

				// 컴포넌트에 스키마 등록
				.components(new Components()
						.addSecuritySchemes(cookieAuth, cookieScheme)
						.addSecuritySchemes(oauth2Kakao, kakaoScheme)
						.addSecuritySchemes(oauth2Naver, naverScheme));
	}
}
