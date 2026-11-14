package com.nexus.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusOpenAPI() {
        final String securitySchemeName = "cookieAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("NEXUS Opportunity Intelligence API")
                        .description("REST API documentation for NEXUS Phase 1: Foundation, Authentication, Profiles and Skills.")
                        .version("1.0.0")
                        .contact(new Contact().name("NEXUS Core Team"))
                        .license(new License().name("Apache 2.0")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name("nexus_session")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .description("HttpOnly Session Cookie")));
    }
}
