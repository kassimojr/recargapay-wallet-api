package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.DepositUseCase;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import com.recargapay.wallet.core.services.common.TransactionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementação do caso de uso para depósitos em uma carteira
 */
@Service
public class DepositService extends TransactionService implements DepositUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final DepositService self;

    /**
     * Construtor
     * 
     * @param walletRepository repositório de carteiras, não deve ser nulo
     * @param transactionRepository repositório de transações, não deve ser nulo
     * @param self referência para o próprio bean gerenciado pelo Spring (auto-injeção)
     */
    public DepositService(WalletRepository walletRepository, 
                         TransactionRepository transactionRepository,
                         @Lazy DepositService self) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.self = self;
    }

    /**
     * Realiza um depósito em uma carteira, com validações e mecanismo de retry em caso de falha de bloqueio otimista
     *
     * @param walletId o ID da carteira, não deve ser nulo
     * @param amount valor a ser depositado, não deve ser nulo
     * @return a transação de depósito criada
     * @throws IllegalArgumentException se id da carteira for nulo ou valor do depósito for nulo/zero/negativo
     * @throws WalletNotFoundException se a carteira não for encontrada
     */
    @Override
    @Transactional
    public Transaction deposit(UUID walletId, BigDecimal amount) {
        validateDepositParams(walletId, amount);
        logger.info("Iniciando operação de depósito para a carteira {} no valor de {}", walletId, amount);
        return executeWithRetry(() -> self.executeDeposit(walletId, amount), "depósito");
    }
    
    /**
     * Valida os parâmetros de entrada para o depósito
     * 
     * @param walletId ID da carteira
     * @param amount valor do depósito
     * @throws IllegalArgumentException se os parâmetros forem inválidos
     */
    private void validateDepositParams(UUID walletId, BigDecimal amount) {
        if (walletId == null) {
            throw new IllegalArgumentException("ID da carteira não pode ser nulo");
        }
        
        if (amount == null) {
            throw new IllegalArgumentException("Valor de depósito não pode ser nulo");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de depósito deve ser maior que zero");
        }
    }
    
    /**
     * Executa o depósito propriamente dito, atualizando o saldo e criando a transação
     *
     * @param walletId o ID da carteira, não deve ser nulo
     * @param amount valor a ser depositado, não deve ser nulo
     * @return a transação de depósito criada
     * @throws WalletNotFoundException se a carteira não for encontrada
     * @throws IllegalArgumentException se os parâmetros forem inválidos
     */
    @Transactional(noRollbackFor = OptimisticLockingFailureException.class)
    public Transaction executeDeposit(UUID walletId, BigDecimal amount) {
        // Valida novamente os parâmetros por segurança
        if (walletId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Tentativa de depósito com parâmetros inválidos: walletId={}, amount={}", walletId, amount);
            throw new IllegalArgumentException("Parâmetros de depósito inválidos");
        }
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    logger.error("Carteira não encontrada: {}", walletId);
                    return new WalletNotFoundException("Carteira não encontrada: " + walletId);
                });
        
        logger.debug("Carteira encontrada: {} com saldo atual: {}", walletId, wallet.getBalance());
        
        // Atualizando o saldo
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);
        wallet.setBalance(newBalance);
        logger.debug("Atualizando saldo da carteira {} de {} para {}", walletId, oldBalance, newBalance);
        
        Wallet savedWallet = walletRepository.save(wallet);
        
        if (savedWallet == null) {
            logger.error("Falha ao salvar a carteira após depósito: {}", walletId);
            throw new IllegalStateException("Erro ao atualizar saldo da carteira");
        }
        
        logger.debug("Saldo da carteira {} atualizado com sucesso para {}", walletId, savedWallet.getBalance());

        // Criando a transação
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                walletId,
                amount,
                TransactionType.DEPOSIT,
                LocalDateTime.now(),
                wallet.getUserId()
        );
        
        Transaction savedTransaction = transactionRepository.saveAndReturn(transaction);
        if (savedTransaction == null) {
            logger.error("Erro ao salvar a transação de depósito para a carteira {}", walletId);
            throw new IllegalStateException("Erro ao salvar a transação de depósito");
        }
        logger.info("Transação de depósito criada com sucesso para a carteira {}: {}", walletId, savedTransaction);
        return savedTransaction;
    }
}
