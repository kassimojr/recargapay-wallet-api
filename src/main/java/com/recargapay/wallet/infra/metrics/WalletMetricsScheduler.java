package com.recargapay.wallet.infra.metrics;

import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;

/**
 * Scheduled job that periodically updates all wallet balance metrics.
 * This ensures wallet balance gauges are always present in Prometheus even if no transactions occur.
 */
@Configuration
@EnableScheduling
@Slf4j
public class WalletMetricsScheduler {

    private final WalletRepository walletRepository;
    private final WalletGaugeRegistry walletGaugeRegistry;

    @Autowired
    public WalletMetricsScheduler(
            WalletRepository walletRepository, 
            WalletGaugeRegistry walletGaugeRegistry) {
        this.walletRepository = walletRepository;
        this.walletGaugeRegistry = walletGaugeRegistry;
        log.info("Wallet metrics scheduler initialized");
    }
    
    /**
     * Force registration of all wallets as soon as the application starts.
     * This ensures metrics are available immediately.
     */
    @PostConstruct
    public void initializeMetrics() {
        log.info("Initializing wallet balance metrics on startup");
        updateWalletBalanceMetrics();
    }

    /**
     * Updates wallet balance metrics every 15 seconds.
     * More frequent updates increase the chance of metrics being visible during Prometheus scrape.
     */
    @Scheduled(fixedRate = 15000) // Run every 15 seconds for better visibility
    public void updateWalletBalanceMetrics() {
        log.debug("Starting wallet balance metrics update");
        
        try {
            int count = 0;
            for (Wallet wallet : walletRepository.findAll()) {
                walletGaugeRegistry.updateWalletBalance(
                    wallet.getId().toString(), 
                    wallet.getBalance(),
                    "BRL" // Default currency
                );
                count++;
            }
            
            if (count > 0) {
                log.info("Updated {} wallet balance metrics successfully", count);
            } else {
                log.warn("No wallets found for metrics update");
            }
        } catch (Exception e) {
            log.error("Error updating wallet balance metrics: {}", e.getMessage(), e);
        }
    }
}
