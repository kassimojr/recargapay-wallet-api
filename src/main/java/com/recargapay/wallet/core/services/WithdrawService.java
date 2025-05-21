package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.InsufficientBalanceException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.WithdrawUseCase;
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
 * Implementação do caso de uso para saques de uma carteira
 */
@Service
public class WithdrawService extends TransactionService implements WithdrawUseCase {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawService self;

    /**
     * Construtor
     * 
     * @param walletRepository repositório de carteiras, não deve ser nulo
     * @param transactionRepository repositório de transações, não deve ser nulo
     * @param self referência para o próprio bean gerenciado pelo Spring (auto-injeção)
     */
    public WithdrawService(WalletRepository walletRepository, 
                          TransactionRepository transactionRepository,
                          @Lazy WithdrawService self) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.self = self;
    }

    /**
     * Realiza um saque de uma carteira, com validações e mecanismo de retry em caso de falha de bloqueio otimista
     *
     * @param walletId o ID da carteira, não deve ser nulo
     * @param amount valor a ser sacado, não deve ser nulo
     * @return a transação de saque criada
     * @throws IllegalArgumentException se id da carteira for nulo ou valor do saque for nulo/zero/negativo
     * @throws WalletNotFoundException se a carteira não for encontrada
     * @throws InsufficientBalanceException se o saldo for insuficiente
     */
    @Override
    @Transactional
    public Transaction withdraw(UUID walletId, BigDecimal amount) {
        validateWithdrawParams(walletId, amount);
        logger.info("Iniciando operação de saque para a carteira {} no valor de {}", walletId, amount);
        return executeWithRetry(() -> self.executeWithdraw(walletId, amount), "saque");
    }
    
    /**
     * Valida os parâmetros de entrada para o saque
     * 
     * @param walletId ID da carteira, não deve ser nulo
     * @param amount valor do saque, não deve ser nulo
     * @throws IllegalArgumentException se os parâmetros forem inválidos
     */
    private void validateWithdrawParams(UUID walletId, BigDecimal amount) {
        if (walletId == null) {
            throw new IllegalArgumentException("ID da carteira não pode ser nulo");
        }
        
        if (amount == null) {
            throw new IllegalArgumentException("Valor de saque não pode ser nulo");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de saque deve ser maior que zero");
        }
    }
    
    /**
     * Executa o saque propriamente dito, atualizando o saldo e criando a transação
     *
     * @param walletId o ID da carteira, não deve ser nulo
     * @param amount valor a ser sacado, não deve ser nulo
     * @return a transação de saque criada
     * @throws WalletNotFoundException se a carteira não for encontrada
     * @throws InsufficientBalanceException se o saldo for insuficiente
     */
    @Transactional(noRollbackFor = OptimisticLockingFailureException.class)
    public Transaction executeWithdraw(UUID walletId, BigDecimal amount) {
        // Valida novamente os parâmetros por segurança
        if (walletId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Tentativa de saque com parâmetros inválidos: walletId={}, amount={}", walletId, amount);
            throw new IllegalArgumentException("Parâmetros de saque inválidos");
        }
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> {
                    logger.error("Carteira não encontrada: {}", walletId);
                    return new WalletNotFoundException("Carteira não encontrada: " + walletId);
                });
        
        logger.debug("Carteira encontrada: {} com saldo atual: {}", walletId, wallet.getBalance());
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            logger.error("Saldo insuficiente para saque na carteira {}: saldo={}, valor solicitado={}", 
                    walletId, wallet.getBalance(), amount);
            throw new InsufficientBalanceException("Saldo insuficiente para saque");
        }
        
        // Atualizando o saldo
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.subtract(amount);
        wallet.setBalance(newBalance);
        logger.debug("Atualizando saldo da carteira {} de {} para {}", walletId, oldBalance, newBalance);
        
        Wallet savedWallet = walletRepository.save(wallet);
        
        if (savedWallet == null) {
            logger.error("Falha ao salvar a carteira após saque: {}", walletId);
            throw new IllegalStateException("Erro ao atualizar saldo da carteira");
        }
        
        logger.debug("Saldo da carteira {} atualizado com sucesso para {}", walletId, savedWallet.getBalance());

        // Criando a transação
        Transaction transaction = new Transaction(
                UUID.randomUUID(),
                walletId,
                amount.negate(),
                TransactionType.WITHDRAW,
                LocalDateTime.now(),
                wallet.getUserId()
        );
        
        Transaction savedTransaction = transactionRepository.saveAndReturn(transaction);
        if (savedTransaction == null) {
            logger.error("Erro ao salvar a transação de saque para a carteira {}", walletId);
            throw new IllegalStateException("Erro ao salvar a transação de saque");
        }
        logger.info("Transação de saque criada com sucesso para a carteira {}: {}", walletId, savedTransaction);
        return savedTransaction;
    }
}
