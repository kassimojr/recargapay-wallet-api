package com.recargapay.wallet.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;

/**
 * Configuration class to resolve the conflict between multiple ParameterNameDiscoverer beans.
 * This is needed to resolve the conflict between OpenTelemetry and SpringDoc.
 */
@Configuration
public class ParameterNameDiscovererConfig {

    /**
     * Creates a primary ParameterNameDiscoverer bean to be used by the application.
     * This resolves the conflict between beans from OpenTelemetry and SpringDoc.
     *
     * @return A primary ParameterNameDiscoverer bean
     */
    @Bean
    @Primary
    public ParameterNameDiscoverer primaryParameterNameDiscoverer() {
        return new StandardReflectionParameterNameDiscoverer();
    }
}
