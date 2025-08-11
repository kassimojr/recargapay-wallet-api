package com.recargapay.wallet.infra;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration for OpenTelemetry components.
 * 
 * This configuration provides mock implementations of OpenTelemetry
 * components for testing purposes, avoiding the need for full
 * OpenTelemetry setup in test environments.
 */
@TestConfiguration
public class TestOpenTelemetryConfig {

    /**
     * Provides a mock OpenTelemetry instance for tests.
     * 
     * @return mock OpenTelemetry instance with proper propagators
     */
    @Bean
    @Primary
    public OpenTelemetry openTelemetry() {
        OpenTelemetry mockOpenTelemetry = mock(OpenTelemetry.class);
        ContextPropagators mockPropagators = mock(ContextPropagators.class);
        TextMapPropagator mockTextMapPropagator = mock(TextMapPropagator.class);
        
        when(mockOpenTelemetry.getPropagators()).thenReturn(mockPropagators);
        when(mockPropagators.getTextMapPropagator()).thenReturn(mockTextMapPropagator);
        
        return mockOpenTelemetry;
    }

    /**
     * Provides a mock Tracer instance for tests.
     * 
     * @return mock Tracer instance
     */
    @Bean
    @Primary
    public Tracer tracer() {
        return mock(Tracer.class);
    }
}
