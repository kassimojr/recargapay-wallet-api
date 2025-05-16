package com.recargapay.wallet.core.ports.out;

import com.recargapay.wallet.core.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID userId);
    Optional<User> findByEmail(String email);
    User save(User user);
    void update(User user);
    void delete(UUID userId);
    List<User> findAll();
}
