package com.recargapay.wallet.infra.health;

import com.recargapay.wallet.core.ports.in.FindAllWalletsUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Custom health indicator for the Wallet Service.
 * Checks if the service is operating correctly by performing a lightweight fetch operation.
 */
@Component
public class WalletServiceHealthIndicator implements HealthIndicator {

    private final FindAllWalletsUseCase findAllWalletsUseCase;

    @Autowired
    public WalletServiceHealthIndicator(FindAllWalletsUseCase findAllWalletsUseCase) {
        this.findAllWalletsUseCase = findAllWalletsUseCase;
    }

    @Override
    public Health health() {
        try {
            Instant start = Instant.now();
            
            // Fetch the first wallet as a lightweight operation to check service functionality
            long walletCount = findAllWalletsUseCase.findAll().size();
            
            Duration duration = Duration.between(start, Instant.now());
            
            return Health.up()
                .withDetail("service", "Wallet")
                .withDetail("walletCount", walletCount)
                .withDetail("responseTimeMs", duration.toMillis())
                .build();
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return Health.down()
                .withDetail("service", "Wallet")
                .withDetail("error", errorMessage)
                .build();
        }
    }
}
