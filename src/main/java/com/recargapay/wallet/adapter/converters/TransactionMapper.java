package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.TransactionDTO;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.core.domain.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class TransactionMapper {
    private final ModelMapper mapper;

    public TransactionMapper(ModelMapper mapper) {
        this.mapper = mapper;
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
        
        return dto;
    }

    public List<TransactionDTO> toDTOList(List<Transaction> domains) {
        return Objects.requireNonNullElse(domains, List.<Transaction>of()).stream().map(this::toDTO).toList();
    }
}
