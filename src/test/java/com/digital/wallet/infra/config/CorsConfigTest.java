package com.digital.wallet.infra.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CorsConfig Tests")
class CorsConfigTest {

    @Mock
    private CorsRegistry corsRegistry;

    @Mock
    private CorsRegistration corsRegistration;

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        
        // Set default values using reflection
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", new String[]{"http://localhost:3000", "http://localhost:8080"});
        ReflectionTestUtils.setField(corsConfig, "allowedMethods", new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"});
        ReflectionTestUtils.setField(corsConfig, "allowedHeaders", new String[]{"*"});
        ReflectionTestUtils.setField(corsConfig, "allowCredentials", true);
        ReflectionTestUtils.setField(corsConfig, "maxAge", 3600L);
    }

    @Test
    @DisplayName("Should configure CORS mappings for all endpoints")
    void shouldConfigureCorsForAllEndpoints() {
        // Given
        when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(anyLong())).thenReturn(corsRegistration);

        // When
        corsConfig.addCorsMappings(corsRegistry);

        // Then
        verify(corsRegistry).addMapping("/api/**");
        verify(corsRegistry).addMapping("/v3/api-docs/**");
        verify(corsRegistry).addMapping("/swagger-ui/**");
        verify(corsRegistration, atLeastOnce()).allowedOrigins("http://localhost:3000", "http://localhost:8080");
        verify(corsRegistration, atLeastOnce()).allowedHeaders("*");
        verify(corsRegistration, atLeastOnce()).maxAge(3600L);
    }

    @Test
    @DisplayName("Should create CorsConfig instance")
    void shouldCreateCorsConfigInstance() {
        // Given & When
        CorsConfig config = new CorsConfig();

        // Then
        // Constructor execution is tested by successful instantiation
        // No assertions needed as constructor doesn't return anything
    }

    @Test
    @DisplayName("Should configure CORS with proper method calls")
    void shouldConfigureCorsWithProperMethodCalls() {
        // Given
        when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(anyBoolean())).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(anyLong())).thenReturn(corsRegistration);

        // When
        corsConfig.addCorsMappings(corsRegistry);

        // Then
        verify(corsRegistry, times(3)).addMapping(anyString());
        verify(corsRegistration, times(3)).allowedOrigins(any(String[].class));
        verify(corsRegistration, times(3)).allowedHeaders(any(String[].class));
        verify(corsRegistration, times(3)).maxAge(anyLong());
    }
}
