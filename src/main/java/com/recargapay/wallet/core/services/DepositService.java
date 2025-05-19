package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.TransactionType;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.DepositUseCase;
import com.recargapay.wallet.core.ports.out.TransactionRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositService implements DepositUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Transaction deposit(UUID walletId, BigDecimal amount) {
        if (walletId == null) {
            throw new IllegalArgumentException("ID da carteira não pode ser nulo");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de depósito deve ser maior que zero");
        }
        
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada: " + walletId));
        
        // Atualizando o saldo
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // Criando a transação
        Transaction transaction = new Transaction(
                null,  // Deixando o ID como null para que o Hibernate gere automaticamente
                walletId,
                amount,
                TransactionType.DEPOSIT,
                LocalDateTime.now(),
                wallet.getUserId()
        );
        
        return transactionRepository.saveAndReturn(transaction);
    }
}
