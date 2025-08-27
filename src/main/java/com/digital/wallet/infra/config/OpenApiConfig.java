package com.digital.wallet.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OpenApiConfig implements WebMvcConfigurer {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Wallet API")
                        .version("1.0")
                        .description("API for digital wallet management and financial transactions")
                        .contact(new Contact()
                                .name("Digital Wallet Team")
                                .email("api@digitalwallet.com")
                                .url("https://developer.digitalwallet.com"))
                        .license(new License()
                                .name("Proprietary license")
                                .url("https://digitalwallet.com/terms")))
                .externalDocs(new ExternalDocumentation()
                        .description("Complete documentation")
                        .url("https://docs.digitalwallet.com"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                            new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained through the login endpoint")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
    
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**")
                .displayName("Authentication API")
                .build();
    }
    
    @Bean
    public GroupedOpenApi transactionsApi() {
        return GroupedOpenApi.builder()
                .group("transactions")
                .packagesToScan("com.digital.wallet.adapter.controllers.v1")
                .pathsToMatch("/api/v1/transactions/**")
                .displayName("Transactions API")
                .build();
    }
    
    @Bean
    public GroupedOpenApi walletsApi() {
        return GroupedOpenApi.builder()
                .group("wallets")
                .packagesToScan("com.digital.wallet.adapter.controllers.v1")
                .pathsToMatch("/api/v1/wallets/**")
                .displayName("Wallets API")
                .build();
    }
    
    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .packagesToScan("com.digital.wallet.adapter.controllers.v1")
                .pathsToMatch("/api/v1/users/**")
                .displayName("Users API")
                .build();
    }
    
    @Bean
    public GroupedOpenApi allApisV1() {
        return GroupedOpenApi.builder()
                .group("wallet-v1")
                .packagesToScan("com.digital.wallet.adapter.controllers")
                .pathsToMatch("/api/v1/**")
                .displayName("Wallets API V1 (All)")
                .build();
    }
}
