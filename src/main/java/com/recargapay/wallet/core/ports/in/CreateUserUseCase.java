package com.recargapay.wallet.core.ports.in;

import com.recargapay.wallet.core.domain.User;
import java.util.Optional;
import java.util.UUID;

public interface CreateUserUseCase {
    User create(User user);
    Optional<User> findById(UUID userId);
    Optional<User> findByEmail(String email);
}
