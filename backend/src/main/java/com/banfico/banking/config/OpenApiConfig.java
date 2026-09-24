package com.banfico.banking.config;

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
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banfico Mini Banking / Open Banking Consent Management API")
                        .description("REST APIs for Banfico Full-Stack Training Program: Core Banking, Accounts, Transactions, Beneficiaries & Open Banking Consents.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Banfico Engineering Team")
                                .email("engineering@banfico.com")
                                .url("https://banfico.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList("Keycloak Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Keycloak Bearer Token", new SecurityScheme()
                                .name("Keycloak Bearer Token")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter Keycloak JWT access token from /auth/realms/banfico-realm/protocol/openid-connect/token")));
    }
}
