package com.recargapay.wallet.infra.logging;

import com.recargapay.wallet.core.ports.out.DomainLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LoggingUtils.log(logger, operation + "_START", 
                "walletId", walletId, 
                "amount", amount, 
                "currency", "BRL");
    }
    
    @Override
    public void logTransferStart(String operation, String fromWalletId, String toWalletId, String amount) {
        LoggingUtils.log(logger, operation + "_START", 
                "fromWalletId", fromWalletId, 
                "toWalletId", toWalletId, 
                "amount", amount, 
                "currency", "BRL");
    }
    
    @Override
    public void logOperationSuccess(String operation, String walletId, String amount, String transactionId) {
        LoggingUtils.log(logger, operation + "_SUCCESS", 
                "walletId", walletId, 
                "amount", amount, 
                "transactionId", transactionId);
    }
    
    @Override
    public void logTransferSuccess(String operation, String fromWalletId, String toWalletId, String amount, String transactionId) {
        LoggingUtils.log(logger, operation + "_SUCCESS", 
                "fromWalletId", fromWalletId, 
                "toWalletId", toWalletId, 
                "amount", amount, 
                "transactionId", transactionId);
    }
    
    @Override
    public void logOperationError(String operation, String walletId, String errorType, String errorMessage) {
        LoggingUtils.log(logger, operation + "_ERROR", 
                "walletId", walletId, 
                "errorType", errorType, 
                "errorMessage", errorMessage);
    }
}
