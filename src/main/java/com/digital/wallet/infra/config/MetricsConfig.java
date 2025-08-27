package com.digital.wallet.infra.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for application metrics.
 * Configures Micrometer and Spring Boot Actuator for Prometheus metrics collection.
 */
@Configuration
public class MetricsConfig {

    /**
     * Customizes the MeterRegistry with common tags for better metrics categorization.
     * 
     * @param environment Spring Environment for accessing profiles
     * @return A customizer for the MeterRegistry
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
            Environment environment,
            @Value("${spring.application.name:wallet-api}") String applicationName) {
        
        return registry -> {
            // Add common tags that will be applied to all metrics
            List<Tag> tags = List.of(
                Tag.of("application", applicationName),
                Tag.of("environment", Arrays.toString(environment.getActiveProfiles()))
            );
            
            registry.config().commonTags(tags);
        };
    }

    /**
     * Creates a TimedAspect bean to enable @Timed annotations for methods.
     * This will automatically track method execution times.
     * 
     * @param registry The MeterRegistry to which timers will be registered
     * @return The TimedAspect bean
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
