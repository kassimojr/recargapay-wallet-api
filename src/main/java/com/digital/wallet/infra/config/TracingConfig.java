package com.digital.wallet.infra.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for distributed tracing in the application.
 * Sets up necessary beans for observability and tracing.
 */
@Configuration
public class TracingConfig {
    
    /**
     * Creates the ObservedAspect bean to enable the @Observed annotation
     * for method-level tracing with Micrometer Observation API.
     * 
     * @param observationRegistry the registry for storing observations
     * @return a new ObservedAspect instance
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
