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
		// JWT 설정
		String jwt = "JWT";
		SecurityRequirement jwtSecurityRequirement = new SecurityRequirement().addList(jwt);
		SecurityScheme jwtScheme = new SecurityScheme()
				.name(jwt)
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT");

		// OAuth2 설정 1
		String oauth2Kakao = "OAuth2_Kakao";
		SecurityRequirement kakaoSecurityRequirement = new SecurityRequirement().addList(oauth2Kakao);
		SecurityScheme kakaoScheme = new SecurityScheme()
				.name(oauth2Kakao)
				.type(SecurityScheme.Type.OAUTH2)
				.flows(new OAuthFlows()
						.authorizationCode(new OAuthFlow()
								.authorizationUrl("http://localhost:8080/oauth2/kakao/authorize")
								.tokenUrl("http://localhost:8080/oauth2/kakao/token")
								.scopes(new Scopes()
										.addString("client_id", kakaoClientId)
										.addString("client_secret", kakaoClientSecret))));

		// OAuth2 설정 2
		String oauth2Naver = "OAuth2_Naver";
		SecurityRequirement naverSecurityRequirement = new SecurityRequirement().addList(oauth2Naver);
		SecurityScheme naverScheme = new SecurityScheme()
				.name(oauth2Naver)
				.type(SecurityScheme.Type.OAUTH2)
				.flows(new OAuthFlows()
						.authorizationCode(new OAuthFlow()
								.authorizationUrl("http://localhost:8080/oauth2/naver/authorize")
								.tokenUrl("http://localhost:8080/oauth2/naver/token")
								.scopes(new Scopes()
										.addString("client_id", naverClientId)
										.addString("client_secret", naverClientSecret))));

		// 서버 설정
		Server server = new Server();
		server.setUrl("/");

		// OpenAPI 구성
		return new OpenAPI()
				.servers(List.of(server))
				.addSecurityItem(jwtSecurityRequirement)
				.addSecurityItem(kakaoSecurityRequirement)
				.addSecurityItem(naverSecurityRequirement)
				.components(new Components()
						.addSecuritySchemes(jwt, jwtScheme)
						.addSecuritySchemes(oauth2Kakao, kakaoScheme)
						.addSecuritySchemes(oauth2Naver, naverScheme));
	}
}