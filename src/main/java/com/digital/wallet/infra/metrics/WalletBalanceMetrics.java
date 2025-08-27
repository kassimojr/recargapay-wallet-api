package com.digital.wallet.infra.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.digital.wallet.infra.metrics.MetricsConstants.*;

/**
 * This component manages wallet balance metrics in a way that is compatible with
 * Prometheus and Micrometer's gauge implementation.
 * 
 * It maintains a stable reference to wallet balances that persists throughout
 * the application's lifecycle, ensuring metrics are properly collected.
 */
@Component
@Slf4j
public class WalletBalanceMetrics {

    private final MeterRegistry meterRegistry;
    
    // Map that holds the latest balance for each wallet_id:currency combination
    private final Map<String, AtomicReference<BigDecimal>> walletBalances = new ConcurrentHashMap<>();
    
    @Autowired
    public WalletBalanceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * Initialize base metrics during component startup.
     * This ensures the metrics appear in Prometheus even if no transactions have occurred.
     */
    @PostConstruct
    public void initialize() {
        log.info("Initializing wallet balance metrics component");
    }
    
    /**
     * Updates the balance for a specific wallet and currency.
     * The gauge metric will reflect this value on the next Prometheus scrape.
     *
     * @param walletId ID of the wallet
     * @param balance Current balance
     * @param currency Currency code
     */
    public void updateBalance(String walletId, BigDecimal balance, String currency) {
        String key = getKey(walletId, currency);
        
        // Get or create the AtomicReference for this wallet+currency
        AtomicReference<BigDecimal> balanceRef = walletBalances.computeIfAbsent(key, k -> {
            AtomicReference<BigDecimal> newRef = new AtomicReference<>(balance);
            
            // Register a new gauge with this reference (only happens once per wallet+currency)
            meterRegistry.gauge(WALLET_BALANCE, 
                Tags.of(TAG_WALLET_ID, walletId, TAG_CURRENCY, currency),
                newRef,
                ref -> ref.get().doubleValue());
                
            log.info("Registered new wallet_balance gauge for wallet: {}, currency: {}", walletId, currency);
            return newRef;
        });
        
        // Update the existing reference with the new balance
        balanceRef.set(balance);
        log.debug("Updated wallet_balance for wallet: {}, currency: {}, balance: {}", walletId, currency, balance);
    }
    
    /**
     * Creates a unique key for a wallet and currency combination
     */
    private String getKey(String walletId, String currency) {
        return walletId + ":" + currency;
    }
}
