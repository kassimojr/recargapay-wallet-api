package com.digital.wallet.infra.config;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("OpenTelemetryConfig Tests")
class OpenTelemetryConfigTest {

    @Test
    @DisplayName("Should create OpenTelemetry bean")
    void shouldCreateOpenTelemetryBean() {
        // Given
        OpenTelemetryConfig config = new OpenTelemetryConfig();
        
        // When
        OpenTelemetry openTelemetry = config.openTelemetry();
        
        // Then
        assertNotNull(openTelemetry);
        assertNotNull(openTelemetry.getTracerProvider());
    }

    @Test
    @DisplayName("Should create tracer from OpenTelemetry instance")
    void shouldCreateTracerFromOpenTelemetryInstance() {
        // Given
        OpenTelemetryConfig config = new OpenTelemetryConfig();
        OpenTelemetry openTelemetry = config.openTelemetry();
        
        // When
        var tracer = openTelemetry.getTracer("test-tracer");
        
        // Then
        assertNotNull(tracer);
    }
}
