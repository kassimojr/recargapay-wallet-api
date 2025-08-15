package com.recargapay.wallet.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenApiConfig Tests")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    @DisplayName("Should create custom OpenAPI configuration")
    void shouldCreateCustomOpenApiConfiguration() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("RecargaPay Wallet API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0");
        assertThat(openAPI.getInfo().getDescription()).isEqualTo("API for digital wallet management and financial transactions");
    }

    @Test
    @DisplayName("Should configure API info with contact details")
    void shouldConfigureApiInfoWithContactDetails() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        Info info = openAPI.getInfo();
        Contact contact = info.getContact();
        assertThat(contact).isNotNull();
        assertThat(contact.getName()).isEqualTo("RecargaPay Team");
        assertThat(contact.getEmail()).isEqualTo("api@recargapay.com");
        assertThat(contact.getUrl()).isEqualTo("https://developer.recargapay.com");
    }

    @Test
    @DisplayName("Should configure API info with license")
    void shouldConfigureApiInfoWithLicense() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        Info info = openAPI.getInfo();
        License license = info.getLicense();
        assertThat(license).isNotNull();
        assertThat(license.getName()).isEqualTo("Proprietary license");
        assertThat(license.getUrl()).isEqualTo("https://recargapay.com/terms");
    }

    @Test
    @DisplayName("Should configure JWT security scheme")
    void shouldConfigureJwtSecurityScheme() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).isNotNull();
        
        SecurityScheme bearerScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(bearerScheme).isNotNull();
        assertThat(bearerScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(bearerScheme.getScheme()).isEqualTo("bearer");
        assertThat(bearerScheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(bearerScheme.getDescription()).isEqualTo("JWT token obtained through the login endpoint");
    }

    @Test
    @DisplayName("Should configure global security requirement")
    void shouldConfigureGlobalSecurityRequirement() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI.getSecurity()).isNotNull();
        assertThat(openAPI.getSecurity()).hasSize(1);
        
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertThat(securityRequirement.get("bearerAuth")).isNotNull();
        assertThat(securityRequirement.get("bearerAuth")).isEmpty();
    }

    @Test
    @DisplayName("Should create wallets API group")
    void shouldCreateWalletsApiGroup() {
        // When
        GroupedOpenApi walletsApi = openApiConfig.walletsApi();

        // Then
        assertThat(walletsApi).isNotNull();
        assertThat(walletsApi.getGroup()).isEqualTo("wallets");
        assertThat(walletsApi.getDisplayName()).isEqualTo("Wallets API");
    }

    @Test
    @DisplayName("Should create transactions API group")
    void shouldCreateTransactionsApiGroup() {
        // When
        GroupedOpenApi transactionsApi = openApiConfig.transactionsApi();

        // Then
        assertThat(transactionsApi).isNotNull();
        assertThat(transactionsApi.getGroup()).isEqualTo("transactions");
        assertThat(transactionsApi.getDisplayName()).isEqualTo("Transactions API");
    }

    @Test
    @DisplayName("Should create users API group")
    void shouldCreateUsersApiGroup() {
        // When
        GroupedOpenApi usersApi = openApiConfig.usersApi();

        // Then
        assertThat(usersApi).isNotNull();
        assertThat(usersApi.getGroup()).isEqualTo("users");
        assertThat(usersApi.getDisplayName()).isEqualTo("Users API");
    }

    @Test
    @DisplayName("Should create all APIs V1 group")
    void shouldCreateAllApisV1Group() {
        // When
        GroupedOpenApi allApisV1 = openApiConfig.allApisV1();

        // Then
        assertThat(allApisV1).isNotNull();
        assertThat(allApisV1.getGroup()).isEqualTo("wallet-v1");
        assertThat(allApisV1.getDisplayName()).isEqualTo("Wallets API V1 (All)");
    }
}
