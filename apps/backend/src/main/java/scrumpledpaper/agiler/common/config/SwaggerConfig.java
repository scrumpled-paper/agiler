package scrumpledpaper.agiler.common.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	@Bean
	public GroupedOpenApi RiotDataGroup() {
		return GroupedOpenApi.builder()
			.group("Agiler")
			.pathsToMatch("/**")
			.packagesToScan("scrumpledpaper.agiler")
			.build();
	}

	@Bean
	public OpenAPI springShopOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Agiler API")
				.version("v1.0")
				.description("Agiler Backend API with JWT Authentication"))
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
			.components(new io.swagger.v3.oas.models.Components()
				.addSecuritySchemes("bearerAuth",
					new SecurityScheme()
						.type(SecurityScheme.Type.HTTP)
						.scheme("bearer")
						.bearerFormat("JWT")
						.description("Enter your JWT token here")));
	}
}
