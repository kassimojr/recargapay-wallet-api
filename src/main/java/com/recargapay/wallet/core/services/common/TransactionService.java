package com.recargapay.wallet.core.services.common;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;

import com.recargapay.wallet.core.exceptions.WalletTransactionException;

/**
 * Classe abstrata para operações de transação com retry para tratar falhas de bloqueio otimista.
 * Fornece mecanismo de reexecução de operações que podem falhar devido a conflitos de concorrência,
 * seguindo o padrão de bloqueio otimista da JPA.
 */
public abstract class TransactionService {
    protected static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long DELAY_FACTOR_MS = 50L;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Executa uma operação com retry em caso de falha de bloqueio otimista.
     *
     * @param operation     A operação a ser executada
     * @param operationName Nome da operação para logging
     * @return O resultado da operação
     * @throws OptimisticLockingFailureException Se a operação falhar após o número máximo de tentativas
     * @throws WalletTransactionException Se ocorrer uma interrupção durante a espera entre tentativas
     */
    protected <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        int attempt = 1;
        
        while (attempt <= MAX_RETRY_ATTEMPTS) {
            try {
                // Execução da operação isolada em um método separado para atender à regra do SonarQube
                return executeSingleAttempt(operation);
            } catch (OptimisticLockingFailureException e) {
                // Se atingiu o número máximo de tentativas, relança a exceção com mais contexto
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    String mensagemErro = String.format("Não foi possível completar a operação de %s após %d tentativas devido a conflitos de concorrência", 
                            operationName, MAX_RETRY_ATTEMPTS);
                    logger.error(mensagemErro, e);
                    throw new OptimisticLockingFailureException(mensagemErro, e);
                }
                
                // Log da tentativa e incremento do contador
                logger.warn("Falha de bloqueio otimista na operação de {}, tentativa {} de {}. Tentando novamente...", 
                        operationName, attempt, MAX_RETRY_ATTEMPTS);
                attempt++;
                
                // Aguarda antes de tentar novamente (com backoff exponencial)
                backoffWait(attempt, operationName);
            }
        }
        
        // Este trecho nunca deveria ser executado em condições normais.
        // Se esta exceção for lançada, indica um bug na lógica de controle do loop acima.
        throw new WalletTransactionException(
                String.format("Estado inconsistente detectado na operação '%s': o controle deveria ter sido encerrado no loop. Este é um bug que deve ser reportado.", 
                        operationName));
    }
    
    /**
     * Executa uma única tentativa da operação.
     * 
     * @param operation A operação a ser executada
     * @return O resultado da operação
     * @throws OptimisticLockingFailureException Se ocorrer uma falha de bloqueio otimista
     */
    private <T> T executeSingleAttempt(Supplier<T> operation) {
        // Isolamos a chamada do lambda em um único ponto de potencial falha
        return operation.get();
    }
    
    /**
     * Implementa a espera com backoff exponencial entre tentativas.
     * 
     * @param attempt Número da tentativa atual
     * @param operationName Nome da operação para mensagens de log
     * @throws WalletTransactionException Se a thread for interrompida durante a espera
     */
    private void backoffWait(int attempt, String operationName) {
        try {
            TimeUnit.MILLISECONDS.sleep(DELAY_FACTOR_MS * attempt);
        } catch (InterruptedException ie) {
            // Restaura a flag de interrupção e encerra a operação
            Thread.currentThread().interrupt();
            throw new WalletTransactionException("Operação de " + operationName + " foi interrompida durante o retry", ie);
        }
    }
}
