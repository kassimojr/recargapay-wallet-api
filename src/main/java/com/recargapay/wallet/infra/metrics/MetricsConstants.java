package com.recargapay.wallet.infra.metrics;

/**
 * Constants used for metrics names and tags across the application.
 * Centralizing metric names ensures consistency and makes future changes easier.
 */
public final class MetricsConstants {
    
    private MetricsConstants() {
        // Utility class, no instantiation
    }
    
    // Metric names
    public static final String TRANSACTION_COUNT = "wallet_transaction_count_total";
    public static final String TRANSACTION_AMOUNT = "wallet_transaction_amount_total";
    public static final String TRANSACTION_DURATION = "wallet_transaction_duration_seconds";
    public static final String TRANSACTION_ERRORS = "wallet_transaction_errors_total";
    public static final String WALLET_BALANCE = "wallet_balance";
    public static final String HTTP_REQUEST_DURATION = "http_request_duration_seconds";
    
    // Common tag keys
    public static final String TAG_OPERATION = "operation";
    public static final String TAG_ERROR = "error";
    public static final String TAG_CURRENCY = "currency";
    public static final String TAG_WALLET_ID = "wallet_id";
    public static final String TAG_ENDPOINT = "endpoint";
    public static final String TAG_APPLICATION = "application";
    public static final String TAG_ENVIRONMENT = "environment";
    
    // Operation values
    public static final String OPERATION_DEPOSIT = "deposit";
    public static final String OPERATION_WITHDRAWAL = "withdrawal";
    public static final String OPERATION_TRANSFER = "transfer";
}
