package com.recargapay.wallet.infra.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that checks the wallet database connection and basic functionality.
 * This indicator will be included in the /actuator/health endpoint.
 */
@Component
public class WalletDatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public WalletDatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            // Test basic database connectivity
            Integer testResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (testResult == null || testResult != 1) {
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", "Database test query failed")
                    .build();
            }
            
            // Test wallet table access and get count
            Integer walletCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
            if (walletCount == null) {
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", "Wallet count query returned null")
                    .build();
            }
            
            return Health.up()
                .withDetail("database", "PostgreSQL")
                .withDetail("walletCount", walletCount)
                .withDetail("status", "Connected")
                .build();
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown database error occurred";
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", errorMessage)
                .build();
        }
    }
}
