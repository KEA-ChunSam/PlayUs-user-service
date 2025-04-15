package com.playus.user_service.global.swagger;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@OpenAPIDefinition(info = @Info(
	title = "PlayUs User Service API",
	description = "PlayUs 사용자 관리 서비스 API 문서",
	version = "v1.0.0"))

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openApi() {
		String jwt = "JWT";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");
		Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
			.name(jwt)
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
		);

		Server server = new Server();
		server.setUrl("/");

		return new OpenAPI()
			.servers(List.of(server))
			.addSecurityItem(securityRequirement)
			.components(components);
	}
}
