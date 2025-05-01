package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.exceptions.InsufficientBalanceException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.TransferFundsUseCase;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class TransferFundsService implements TransferFundsUseCase {
    private static final Logger log = LoggerFactory.getLogger(TransferFundsService.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferFundsService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        log.info("Iniciando transferência de {} de {} para {}", amount, fromWalletId, toWalletId);

        if (amount == null) {
            log.warn("Valor da transferência é nulo");
            throw new IllegalArgumentException("O valor da transferência não pode ser nulo");
        }
        if (fromWalletId.equals(toWalletId)) {
            log.warn("Tentativa de transferência para a mesma carteira: {}", fromWalletId);
            throw new IllegalArgumentException("Não é permitido transferir para a mesma carteira");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Valor de transferência inválido: {}", amount);
            throw new IllegalArgumentException("O valor da transferência deve ser positivo");
        }

        Wallet fromWallet = walletRepository.findById(fromWalletId)
            .orElseThrow(() -> new WalletNotFoundException("Carteira de origem não encontrada"));
        Wallet toWallet = walletRepository.findById(toWalletId)
            .orElseThrow(() -> new WalletNotFoundException("Carteira de destino não encontrada"));

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            log.warn("Saldo insuficiente na carteira {}", fromWalletId);
            throw new InsufficientBalanceException("Saldo insuficiente para transferência");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.update(fromWallet);
        walletRepository.update(toWallet);

        // relatedUserId: para débito, é o usuário da carteira de destino; para crédito, é o usuário da carteira de origem
        Transaction debitTransaction = new Transaction(
            UUID.randomUUID(),
            fromWalletId,
            amount.negate(),
            TransactionType.TRANSFER,
            LocalDateTime.now(),
            toWallet.getUserId()
        );
        Transaction creditTransaction = new Transaction(
            UUID.randomUUID(),
            toWalletId,
            amount,
            TransactionType.TRANSFER,
            LocalDateTime.now(),
            fromWallet.getUserId()
        );

        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);

        log.info("Transferência concluída com sucesso");
    }
}
