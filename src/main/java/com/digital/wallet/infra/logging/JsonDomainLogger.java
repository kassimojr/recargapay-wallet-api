package com.digital.wallet.infra.logging;

import com.digital.wallet.core.ports.out.DomainLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the DomainLogger port that uses structured JSON logging.
 * This adapter connects the domain's logging requirements to the specific logging implementation.
 */
public class JsonDomainLogger implements DomainLogger {
    private final Logger logger;
    
    public JsonDomainLogger(Class<?> loggerClass) {
        this.logger = LoggerFactory.getLogger(loggerClass);
    }
    
    @Override
    public void logOperationStart(String operation, String walletId, String amount) {
        LoggingUtils.log(logger, operation, 
                "status", "START",
                "walletId", walletId, 
                "amount", amount);
    }
    
    @Override
    public void logTransferStart(String operation, String fromWalletId, String toWalletId, String amount) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "START");
        data.put("fromWalletId", fromWalletId);
        data.put("toWalletId", toWalletId);
        data.put("amount", amount);
        data.put("currency", "BRL");
        LoggingUtils.log(logger, operation, data);
    }
    
    @Override
    public void logOperationSuccess(String operation, String walletId, String amount, String transactionId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "SUCCESS");
        data.put("walletId", walletId);
        data.put("amount", amount);
        data.put("transactionId", transactionId);
        data.put("currency", "BRL");
        LoggingUtils.log(logger, operation, data);
    }
    
    @Override
    public void logTransferSuccess(String operation, String fromWalletId, String toWalletId, String amount, String transactionId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "SUCCESS");
        data.put("fromWalletId", fromWalletId);
        data.put("toWalletId", toWalletId);
        data.put("amount", amount);
        data.put("transactionId", transactionId);
        data.put("currency", "BRL");
        LoggingUtils.log(logger, operation, data);
    }
    
    @Override
    public void logOperationError(String operation, String walletId, String errorType, String errorMessage) {
        LoggingUtils.log(logger, operation, 
                "status", "ERROR",
                "walletId", walletId, 
                "errorType", errorType);
    }
}
