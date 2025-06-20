package com.recargapay.wallet.infra.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.recargapay.wallet.infra.metrics.MetricsConstants.*;

/**
 * Service responsible for collecting and exposing metrics for the Wallet API.
 * This class provides methods to track transaction counts, amounts, and timings
 * that will be exposed to Prometheus.
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final String defaultCurrency;
    
    // Counters for tracking different transaction types
    private final Counter depositCounter;
    private final Counter withdrawalCounter;
    private final Counter transferCounter;
    private final Counter errorCounter;
    
    // Timers for tracking transaction durations
    private final Timer depositTimer;
    private final Timer withdrawalTimer;
    private final Timer transferTimer;

    private final WalletGaugeRegistry walletGaugeRegistry;

    @Autowired
    public MetricsService(
            MeterRegistry meterRegistry,
            @Value("${wallet.metrics.default-currency:BRL}") String defaultCurrency,
            WalletGaugeRegistry walletGaugeRegistry) {
        this.meterRegistry = meterRegistry;
        this.defaultCurrency = defaultCurrency;
        this.walletGaugeRegistry = walletGaugeRegistry;
        
        // Initialize transaction counters
        this.depositCounter = Counter.builder(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .description("Total number of deposit transactions")
                .register(meterRegistry);
                
        this.withdrawalCounter = Counter.builder(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .description("Total number of withdrawal transactions")
                .register(meterRegistry);
                
        this.transferCounter = Counter.builder(TRANSACTION_COUNT)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .description("Total number of transfer transactions")
                .register(meterRegistry);
                
        this.errorCounter = Counter.builder(TRANSACTION_ERRORS)
                .description("Total number of transaction errors")
                .register(meterRegistry);
                
        // Initialize transaction timers
        this.depositTimer = Timer.builder(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_DEPOSIT)
                .description("Time taken to process deposit transactions")
                .register(meterRegistry);
                
        this.withdrawalTimer = Timer.builder(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_WITHDRAWAL)
                .description("Time taken to process withdrawal transactions")
                .register(meterRegistry);
                
        this.transferTimer = Timer.builder(TRANSACTION_DURATION)
                .tag(TAG_OPERATION, OPERATION_TRANSFER)
                .description("Time taken to process transfer transactions")
                .register(meterRegistry);
                
        log.info("Metrics service initialized with Prometheus registry (default currency: {})", defaultCurrency);
    }

    /**
     * Records a successful deposit transaction.
     *
     * @param amount The amount that was deposited
     * @param durationMs How long the transaction took in milliseconds
     */
    public void recordDepositTransaction(BigDecimal amount, long durationMs) {
        depositCounter.increment();
        depositTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        // Also record the amount as a separate metric with dynamic tags
        meterRegistry.counter(TRANSACTION_AMOUNT, 
                TAG_OPERATION, OPERATION_DEPOSIT,
                TAG_CURRENCY, defaultCurrency)
                .increment(amount.doubleValue());
                
        log.debug("Recorded deposit transaction: amount={}, duration={}ms", amount, durationMs);
    }

    /**
     * Records a successful deposit transaction with a specified currency.
     *
     * @param amount The amount that was deposited
     * @param durationMs How long the transaction took in milliseconds
     * @param currency The currency of the transaction
     */
    public void recordDepositTransaction(BigDecimal amount, long durationMs, String currency) {
        depositCounter.increment();
        depositTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        meterRegistry.counter(TRANSACTION_AMOUNT, 
                TAG_OPERATION, OPERATION_DEPOSIT,
                TAG_CURRENCY, currency)
                .increment(amount.doubleValue());
                
        log.debug("Recorded deposit transaction: amount={}, duration={}ms, currency={}", 
                amount, durationMs, currency);
    }

    /**
     * Records a successful withdrawal transaction.
     *
     * @param amount The amount that was withdrawn
     * @param durationMs How long the transaction took in milliseconds
     */
    public void recordWithdrawalTransaction(BigDecimal amount, long durationMs) {
        withdrawalCounter.increment();
        withdrawalTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        // Also record the amount as a separate metric
        meterRegistry.counter(TRANSACTION_AMOUNT, 
                TAG_OPERATION, OPERATION_WITHDRAWAL,
                TAG_CURRENCY, defaultCurrency)
                .increment(amount.doubleValue());
                
        log.debug("Recorded withdrawal transaction: amount={}, duration={}ms", amount, durationMs);
    }

    /**
     * Records a successful withdrawal transaction with a specified currency.
     *
     * @param amount The amount that was withdrawn
     * @param durationMs How long the transaction took in milliseconds
     * @param currency The currency of the transaction
     */
    public void recordWithdrawalTransaction(BigDecimal amount, long durationMs, String currency) {
        withdrawalCounter.increment();
        withdrawalTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        meterRegistry.counter(TRANSACTION_AMOUNT, 
                TAG_OPERATION, OPERATION_WITHDRAWAL,
                TAG_CURRENCY, currency)
                .increment(amount.doubleValue());
                
        log.debug("Recorded withdrawal transaction: amount={}, duration={}ms, currency={}", 
                amount, durationMs, currency);
    }

    /**
     * Records a successful transfer transaction between wallets.
     *
     * @param amount The amount that was transferred
     * @param durationMs How long the transaction took in milliseconds
     */
    public void recordTransferTransaction(BigDecimal amount, long durationMs) {
        transferCounter.increment();
        transferTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        // Also record the amount as a separate metric
        meterRegistry.counter(TRANSACTION_AMOUNT, 
                TAG_OPERATION, OPERATION_TRANSFER,
                TAG_CURRENCY, defaultCurrency)
                .increment(amount.doubleValue());
                
        log.debug("Recorded transfer transaction: amount={}, duration={}ms", amount, durationMs);
    }

    /**
     * Records a successful transfer transaction between wallets with a specified currency.
     *
     * @param amount The amount that was transferred
     * @param durationMs How long the transaction took in milliseconds
     * @param currency The currency of the transaction
     */
    public void recordTransferTransaction(BigDecimal amount, long durationMs, String currency) {
        transferCounter.increment();
        transferTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        meterRegistry.counter(TRANSACTION_AMOUNT, 
                TAG_OPERATION, OPERATION_TRANSFER,
                TAG_CURRENCY, currency)
                .increment(amount.doubleValue());
                
        log.debug("Recorded transfer transaction: amount={}, duration={}ms, currency={}", 
                amount, durationMs, currency);
    }

    /**
     * Records a failed transaction.
     *
     * @param operation The type of operation that failed (deposit, withdrawal, transfer)
     * @param errorType The type of error that occurred
     */
    public void recordTransactionError(String operation, String errorType) {
        meterRegistry.counter(TRANSACTION_ERRORS, 
                TAG_OPERATION, operation,
                TAG_ERROR, errorType)
                .increment();
                
        log.debug("Recorded transaction error: operation={}, errorType={}", operation, errorType);
    }

    /**
     * Records the current balance for a wallet.
     *
     * @param walletId The ID of the wallet
     * @param balance The current balance
     */
    public void recordWalletBalance(String walletId, BigDecimal balance) {
        walletGaugeRegistry.updateWalletBalance(walletId, balance, defaultCurrency);
    }

    /**
     * Records the current balance for a wallet with a specified currency.
     *
     * @param walletId The ID of the wallet
     * @param balance The current balance
     * @param currency The currency of the balance
     */
    public void recordWalletBalance(String walletId, BigDecimal balance, String currency) {
        walletGaugeRegistry.updateWalletBalance(walletId, balance, currency);
    }
}
