package com.digital.wallet.infra.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry configuration for distributed tracing.
 * <p>
 * This configuration sets up OpenTelemetry SDK with:
 * - Manual tracer provider configuration
 * - Context propagation for distributed tracing
 * - Resource identification for the service
 * - No external span export (logs only)
 */
@Configuration
public class OpenTelemetryConfig {

    /**
     * Creates and configures the OpenTelemetry SDK instance.
     * <p>
     * This configuration:
     * - Sets up the service resource identification
     * - Configures context propagators for distributed tracing
     * - Uses in-memory span processing (no external export)
     * - Enables trace context propagation to MDC for logging
     */
    @Bean
    public OpenTelemetry openTelemetry() {
        // Define service resource
        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.of(
                                ResourceAttributes.SERVICE_NAME, "digital-wallet-api",
                                ResourceAttributes.SERVICE_VERSION, "1.0.0"
                        )
                ));

        // Create tracer provider with no external exporter
        // This keeps spans in memory for MDC propagation but doesn't send to external collectors
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                // No span processors added - spans are created for MDC but not exported
                .build();

        // Configure context propagators for distributed tracing
        // Using W3C standard propagators that are available in the core API
        ContextPropagators contextPropagators = ContextPropagators.create(
                TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance()
                )
        );

        // Build and return OpenTelemetry SDK
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(contextPropagators)
                .build();
    }
}
