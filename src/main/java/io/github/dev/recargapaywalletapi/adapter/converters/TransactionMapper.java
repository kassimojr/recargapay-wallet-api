package io.github.dev.recargapaywalletapi.adapter.converters;

import io.github.dev.recargapaywalletapi.adapter.dtos.TransactionDTO;
import io.github.dev.recargapaywalletapi.adapter.entities.TransactionEntity;
import io.github.dev.recargapaywalletapi.core.domain.Transaction;
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
