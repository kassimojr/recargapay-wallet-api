package com.recargapay.wallet.infra.config;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for health checks in the application.
 * Organizes health indicators into logical groups for better observability.
 */
@Configuration
public class HealthCheckConfig {

    /**
     * Groups wallet service health indicators together.
     *
     * @param walletDatabaseHealthIndicator Database health indicator
     * @param walletServiceHealthIndicator Service functionality health indicator
     * @return A composite health contributor for the wallet component
     */
    @Bean
    public CompositeHealthContributor walletHealthContributor(
            HealthIndicator walletDatabaseHealthIndicator,
            HealthIndicator walletServiceHealthIndicator) {
        
        Map<String, HealthContributor> contributors = new LinkedHashMap<>();
        contributors.put("database", walletDatabaseHealthIndicator);
        contributors.put("service", walletServiceHealthIndicator);
        
        return CompositeHealthContributor.fromMap(contributors);
    }
}
