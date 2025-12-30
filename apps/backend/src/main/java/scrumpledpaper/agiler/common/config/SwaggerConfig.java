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
	public GroupedOpenApi agilerApiGroup() {
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
				.description("Agiler Backend API with JWT (Cookie) and X-API-KEY Authentication"))
			// X-API-KEY 인증 (internal API용)
			.addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"))
			.components(new io.swagger.v3.oas.models.Components()
				// X-API-KEY 헤더 인증
				.addSecuritySchemes("apiKeyAuth",
					new SecurityScheme()
						.type(SecurityScheme.Type.APIKEY)
						.in(SecurityScheme.In.HEADER)
						.name("X-API-KEY")
						.description("Internal API Key")));
	}
}
