package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.TransactionDTO;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.ports.out.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class TransactionMapper {
    
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(TransactionMapper.class);

    public TransactionMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setId(entity.getId());
        transaction.setAmount(entity.getAmount());
        transaction.setWalletId(entity.getWallet() != null ? entity.getWallet().getId() : null);
        transaction.setType(entity.getType());
        transaction.setTimestamp(entity.getTimestamp());
        transaction.setRelatedUserId(entity.getRelatedUserId());
        
        return transaction;
    }

    public TransactionDTO toDTO(Transaction domain) {
        if (domain == null) {
            return null;
        }
        
        TransactionDTO dto = new TransactionDTO();
        dto.setId(domain.getId());
        dto.setWalletId(domain.getWalletId());
        dto.setAmount(domain.getAmount());
        dto.setType(domain.getType() != null ? domain.getType().toString() : null);
        dto.setTimestamp(domain.getTimestamp());
        dto.setRelatedUserId(domain.getRelatedUserId());
        
        // Fetch and add the related user's name
        if (domain.getRelatedUserId() != null) {
            try {
                Optional<User> relatedUser = userRepository.findById(domain.getRelatedUserId());
                relatedUser.ifPresent(user -> dto.setRelatedUserName(user.getName()));
            } catch (Exception e) {
                logger.warn("Error fetching user information: {}", e.getMessage());
                // In case of error, we continue without the username
            }
        }
        
        return dto;
    }

    public List<TransactionDTO> toDTOList(List<Transaction> domains) {
        return Objects.requireNonNullElse(domains, List.<Transaction>of()).stream().map(this::toDTO).toList();
    }
}
