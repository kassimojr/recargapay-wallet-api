package com.recargapay.wallet.core.services.common;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

import com.recargapay.wallet.core.exceptions.WalletTransactionException;

/**
 * Abstract class for transaction operations with retry mechanism to handle optimistic locking failures.
 * Provides a mechanism for re-executing operations that may fail due to concurrency conflicts,
 * following the JPA optimistic locking pattern.
 */
public abstract class TransactionService {
    protected static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long DELAY_FACTOR_MS = 50L;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Executes an operation with retry in case of optimistic locking failure.
     *
     * @param operation     The operation to be executed
     * @param operationName Operation name for logging
     * @return The result of the operation
     * @throws OptimisticLockingFailureException If the operation fails after the maximum number of attempts
     * @throws WalletTransactionException If an interruption occurs during the wait between attempts
     */
    protected <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        int attempt = 1;
        
        while (attempt <= MAX_RETRY_ATTEMPTS) {
            try {
                // Execution of the isolated operation in a separate method to comply with SonarQube rule
                return executeSingleAttempt(operation);
            } catch (OptimisticLockingFailureException e) {
                // If the maximum number of attempts has been reached, rethrow the exception with more context
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    String errorMessage = String.format("Could not complete the %s operation after %d attempts due to concurrency conflicts", 
                            operationName, MAX_RETRY_ATTEMPTS);
                    logger.error(errorMessage, e);
                    throw new OptimisticLockingFailureException(errorMessage, e);
                }
                
                // Log the attempt and increment the counter
                logger.warn("Optimistic locking failure in {} operation, attempt {} of {}. Retrying...", 
                        operationName, attempt, MAX_RETRY_ATTEMPTS);
                attempt++;
                
                // Wait before trying again (with exponential backoff)
                backoffWait(attempt, operationName);
            }
        }
        
        // This code should never be executed under normal conditions.
        // If this exception is thrown, it indicates a bug in the control logic of the above loop.
        throw new WalletTransactionException(
                String.format("Inconsistent state detected in operation '%s': control should have been terminated in the loop. This is a bug that should be reported.", 
                        operationName));
    }
    
    /**
     * Executes a single attempt of the operation.
     * 
     * @param operation The operation to be executed
     * @return The result of the operation
     * @throws OptimisticLockingFailureException If an optimistic locking failure occurs
     */
    private <T> T executeSingleAttempt(Supplier<T> operation) {
        // We isolate the lambda call at a single point of potential failure
        return operation.get();
    }
    
    /**
     * Implements exponential backoff wait between attempts.
     * 
     * @param attempt Current attempt number
     * @param operationName Operation name for log messages
     * @throws WalletTransactionException If the thread is interrupted during the wait
     */
    private void backoffWait(int attempt, String operationName) {
        long waitTime = DELAY_FACTOR_MS * (long) Math.pow(2, attempt - 1);
        try {
            logger.debug("Waiting for {} ms before attempt {} of {}", waitTime, attempt, MAX_RETRY_ATTEMPTS);
            TimeUnit.MILLISECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WalletTransactionException(
                    String.format("Operation '%s' was interrupted during backoff wait between attempts", operationName), e);
        }
    }
}
