package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.UserNotFoundException;
import com.recargapay.wallet.core.exceptions.WalletAlreadyExistsException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.ports.in.CreateWalletUseCase;
import com.recargapay.wallet.core.ports.out.UserRepository;
import com.recargapay.wallet.core.ports.out.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class CreateWalletService implements CreateWalletUseCase {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public CreateWalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Wallet create(Wallet wallet) {
        // Validar se o usuário existe antes de criar a carteira
        if (!userRepository.findById(wallet.getUserId()).isPresent()) {
            throw new UserNotFoundException("Usuário não encontrado: " + wallet.getUserId());
        }
        
        // Verificar se o usuário já possui uma carteira
        if (walletRepository.findByUserId(wallet.getUserId()).isPresent()) {
            throw new WalletAlreadyExistsException("Usuário já possui uma carteira: " + wallet.getUserId());
        }
        
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findById(UUID walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada: " + walletId));
    }

    @Override
    public Wallet findBalanceAt(UUID walletId, String at) {
        // Implementação simplificada: retorna o saldo atual (para produção, buscar transações até o instante 'at')
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada: " + walletId));
    }
}
