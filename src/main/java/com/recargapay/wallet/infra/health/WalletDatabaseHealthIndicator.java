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
            // Execute a simple query to test database connectivity
            int result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            if (result == 1) {
                // Check if wallet table is accessible
                int walletCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
                
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("walletCount", walletCount)
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", "Database test query failed")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
