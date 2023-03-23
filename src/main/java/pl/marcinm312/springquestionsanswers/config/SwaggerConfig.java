package pl.marcinm312.springquestionsanswers.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customizeOpenAPI() {
		final String securitySchemeName = "Bearer Authentication";
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement()
						.addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName, new SecurityScheme()
								.name(securitySchemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("""
										JWT token can be obtained by providing correct username and password in the API by Swagger:
										1. Select a definition: public-apis;
										2. Select controller: login-api-controller;
										3. Select endpoint: /api/login;
										4. Execute request with correct username and password;
										5. Copy Bearer token from authorization response header.
										""")));
	}

	@Bean
	public GroupedOpenApi publicApi() {

		return GroupedOpenApi.builder()
				.group("1. public-apis")
				.pathsToMatch("/api/**")
				.pathsToExclude("/api/actuator/**")
				.build();
	}

	@Bean
	public GroupedOpenApi actuatorApi() {
		return GroupedOpenApi.builder()
				.group("2. actuators")
				.pathsToMatch("/api/actuator/**")
				.build();
	}
}
