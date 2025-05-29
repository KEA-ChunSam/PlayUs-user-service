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

	@Bean
	public OpenAPI openApi() {
		// Cookie 기반 인증 (Access 토큰)
		String cookieAuth = "AccessCookie";
		SecurityRequirement cookieRequirement = new SecurityRequirement().addList(cookieAuth);
		SecurityScheme cookieScheme = new SecurityScheme()
				.name("Access")
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.COOKIE);

		// 서버 기본 URL
		Server server = new Server();
		server.setUrl("/");

		return new OpenAPI()
				.servers(List.of(server))

				// 보안 요구사항 순서대로 등록
				.addSecurityItem(cookieRequirement)

				// 컴포넌트에 스키마 등록
				.components(new Components()
						.addSecuritySchemes(cookieAuth, cookieScheme));
	}
}
