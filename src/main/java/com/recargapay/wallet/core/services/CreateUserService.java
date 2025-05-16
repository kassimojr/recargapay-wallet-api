package com.recargapay.wallet.core.services;

import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.exceptions.DuplicatedResourceException;
import com.recargapay.wallet.core.ports.in.CreateUserUseCase;
import com.recargapay.wallet.core.ports.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CreateUserService implements CreateUserUseCase {
    private final UserRepository userRepository;

    public CreateUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User create(User user) {
        // Verificar se o e-mail já está em uso
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicatedResourceException("E-mail já está em uso: " + user.getEmail());
        }
        
        // Aqui pode-se adicionar validações de negócio, se necessário
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
