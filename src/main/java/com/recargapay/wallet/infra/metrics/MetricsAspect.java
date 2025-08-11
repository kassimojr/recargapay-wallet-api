package com.recargapay.wallet.infra.metrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.recargapay.wallet.infra.metrics.MetricsConstants.*;

/**
 * Aspect for automatically capturing metrics from wallet operations.
 * This aspect intercepts service method calls to record transaction metrics.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsAspect {

    private final MetricsService metricsService;
    
    /**
     * Pointcut that matches deposit operation execution methods.
     */
    @Pointcut("execution(* com.recargapay.wallet.core.usecases.*Deposit*.execute(..)) || " +
              "execution(* com.recargapay.wallet.core.services.*Deposit*.*(..)) || " +
              "execution(* com.recargapay.wallet.adapter.*.deposit*(..))")
    public void depositOperation() {}
    
    /**
     * Pointcut that matches withdrawal operation execution methods.
     */
    @Pointcut("execution(* com.recargapay.wallet.core.usecases.*Withdrawal*.execute(..)) || " +
              "execution(* com.recargapay.wallet.core.services.*Withdrawal*.*(..)) || " +
              "execution(* com.recargapay.wallet.adapter.*.withdraw*(..))")
    public void withdrawalOperation() {}
    
    /**
     * Pointcut that matches transfer operation execution methods.
     */
    @Pointcut("execution(* com.recargapay.wallet.core.usecases.*Transfer*.execute(..)) || " +
              "execution(* com.recargapay.wallet.core.services.*Transfer*.*(..)) || " +
              "execution(* com.recargapay.wallet.adapter.*.transfer*(..))")
    public void transferOperation() {}

    /**
     * Intercepts deposit operations to record metrics.
     * 
     * @param joinPoint The proceeding join point
     * @return The result of the target method execution
     * @throws Throwable if an error occurs during method execution
     */
    @Around("depositOperation()")
    public Object recordDepositMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        return recordTransactionMetrics(joinPoint, OPERATION_DEPOSIT);
    }
    
    /**
     * Intercepts withdrawal operations to record metrics.
     * 
     * @param joinPoint The proceeding join point
     * @return The result of the target method execution
     * @throws Throwable if an error occurs during method execution
     */
    @Around("withdrawalOperation()")
    public Object recordWithdrawalMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        return recordTransactionMetrics(joinPoint, OPERATION_WITHDRAWAL);
    }
    
    /**
     * Intercepts transfer operations to record metrics.
     * 
     * @param joinPoint The proceeding join point
     * @return The result of the target method execution
     * @throws Throwable if an error occurs during method execution
     */
    @Around("transferOperation()")
    public Object recordTransferMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        return recordTransactionMetrics(joinPoint, OPERATION_TRANSFER);
    }
    
    /**
     * Generic method to record transaction metrics.
     * 
     * @param joinPoint The proceeding join point
     * @param operation The type of operation (deposit, withdrawal, transfer)
     * @return The result of the target method execution
     * @throws Throwable if an error occurs during method execution
     */
    private Object recordTransactionMetrics(ProceedingJoinPoint joinPoint, String operation) throws Throwable {
        long startTime = System.currentTimeMillis();
        BigDecimal amount = extractAmount(joinPoint.getArgs());
        String currency = extractCurrency(joinPoint.getArgs());
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Record the transaction based on the operation type
            if (currency != null) {
                // Use currency-specific recording methods if currency was found
                switch (operation) {
                    case OPERATION_DEPOSIT:
                        metricsService.recordDepositTransaction(amount, duration, currency);
                        break;
                    case OPERATION_WITHDRAWAL:
                        metricsService.recordWithdrawalTransaction(amount, duration, currency);
                        break;
                    case OPERATION_TRANSFER:
                        metricsService.recordTransferTransaction(amount, duration, currency);
                        break;
                }
            } else {
                // Use default currency methods if no currency was specified
                switch (operation) {
                    case OPERATION_DEPOSIT:
                        metricsService.recordDepositTransaction(amount, duration);
                        break;
                    case OPERATION_WITHDRAWAL:
                        metricsService.recordWithdrawalTransaction(amount, duration);
                        break;
                    case OPERATION_TRANSFER:
                        metricsService.recordTransferTransaction(amount, duration);
                        break;
                }
            }
            
            return result;
        } catch (Exception e) {
            metricsService.recordTransactionError(operation, e.getClass().getSimpleName());
            throw e;
        }
    }
    
    /**
     * Extracts the transaction amount from method arguments.
     * This method tries to find a BigDecimal argument which is assumed to be the amount.
     * 
     * @param args The method arguments
     * @return The extracted amount, or BigDecimal.ZERO if not found
     */
    private BigDecimal extractAmount(Object[] args) {
        if (args == null || args.length == 0) {
            return BigDecimal.ZERO;
        }
        
        // Try to find a BigDecimal argument
        return Arrays.stream(args)
                .filter(arg -> arg instanceof BigDecimal)
                .map(arg -> (BigDecimal) arg)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("No BigDecimal amount found in arguments: {}", Arrays.toString(args));
                    return BigDecimal.ZERO;
                });
    }
    
    /**
     * Extracts the currency from method arguments if present.
     * This method tries to find a String argument that might represent a currency.
     * 
     * @param args The method arguments
     * @return The extracted currency, or null if not found
     */
    private String extractCurrency(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // Look for String arguments that might be currency codes
        return Arrays.stream(args)
                .filter(arg -> arg instanceof String)
                .map(arg -> (String) arg)
                // Only consider 3-character uppercase strings that might be currency codes
                .filter(str -> str.length() == 3 && str.equals(str.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}
