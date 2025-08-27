package com.digital.wallet.infra.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.digital.wallet.infra.metrics.MetricsConstants.*;

/**
 * Registry for wallet balance gauges that ensures proper tracking in Prometheus.
 * Uses a direct approach with MeterRegistry for reliable metric exposure.
 */
@Component
@Slf4j
public class WalletGaugeRegistry implements MeterBinder {

    private final Map<String, Double> walletBalances = new ConcurrentHashMap<>();
    private MeterRegistry meterRegistry;

    /**
     * Bind method called by Spring Boot when the metrics system is initialized.
     * We don't register any gauges here yet, as we don't know the wallet IDs in advance.
     */
    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
        log.info("Wallet gauge registry bound to meter registry");
    }

    /**
     * Updates the wallet balance metric for a specific wallet and currency.
     * If this is the first time seeing this wallet+currency, a new gauge is registered.
     *
     * @param walletId ID of the wallet
     * @param balance Current balance
     * @param currency Currency code
     */
    public void updateWalletBalance(String walletId, BigDecimal balance, String currency) {
        if (meterRegistry == null) {
            log.warn("MeterRegistry not initialized yet. Skipping metric update.");
            return;
        }
        
        String key = getKey(walletId, currency);
        double doubleValue = balance != null ? balance.doubleValue() : 0.0;
        
        // Update the balance
        walletBalances.put(key, doubleValue);
        
        // Register gauge with fixed ID to avoid duplication
        String metricId = getMetricId(walletId, currency);
        
        // Check if gauge already exists for this wallet/currency
        if (meterRegistry.find(WALLET_BALANCE).tag(TAG_WALLET_ID, walletId).tag(TAG_CURRENCY, currency).gauge() == null) {
            // First time seeing this wallet/currency - register a new gauge
            Gauge.builder(WALLET_BALANCE, walletBalances, map -> map.getOrDefault(key, 0.0))
                .tags(Tags.of(
                    TAG_WALLET_ID, walletId,
                    TAG_CURRENCY, currency
                ))
                .description("Current wallet balance")
                .register(meterRegistry);
                
            log.info("Registered new wallet balance gauge: wallet_id={}, currency={}, balance={}", 
                     walletId, currency, doubleValue);
        } else {
            // Gauge exists, the value will be picked up automatically on next scrape
            log.debug("Updated existing wallet balance: wallet_id={}, currency={}, balance={}", 
                      walletId, currency, doubleValue);
        }
    }
    
    /**
     * Creates a unique key for wallet and currency for the internal map
     */
    private String getKey(String walletId, String currency) {
        return walletId + ":" + currency;
    }
    
    /**
     * Creates a unique metric ID for a wallet and currency
     */
    private String getMetricId(String walletId, String currency) {
        return WALLET_BALANCE + "." + walletId + "." + currency;
    }
}
