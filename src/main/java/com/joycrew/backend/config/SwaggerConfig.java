package com.joycrew.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    final String securitySchemeName = "Authorization";

    return new OpenAPI()
        .servers(List.of(
            new Server().url("https://api.joycrew.co.kr").description("Production Server"),
            new Server().url("http://localhost:8082").description("Local Development Server")
        ))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components().addSecuritySchemes(securitySchemeName,
            new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .info(new Info()
            .title("JoyCrew API")
            .version("v1.0.0")
            .description("JoyCrew Backend API Specification."));
  }
}
