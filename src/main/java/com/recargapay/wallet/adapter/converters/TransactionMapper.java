package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.TransactionDTO;
import com.recargapay.wallet.adapter.entities.TransactionEntity;
import com.recargapay.wallet.core.domain.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    private final ModelMapper mapper;

    public TransactionMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Transaction toDomain(TransactionEntity entity) {
        return mapper.map(entity, Transaction.class);
    }

    public TransactionEntity toEntity(Transaction domain) {
        return mapper.map(domain, TransactionEntity.class);
    }

    public TransactionDTO toDTO(Transaction domain) {
        return mapper.map(domain, TransactionDTO.class);
    }
}
