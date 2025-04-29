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
        return MapperUtils.mapIfNotNull(entity, e -> mapper.map(e, Transaction.class));
    }

    public TransactionEntity toEntity(Transaction domain) {
        return MapperUtils.mapIfNotNull(domain, d -> mapper.map(d, TransactionEntity.class));
    }

    public TransactionDTO toDTO(Transaction domain) {
        return MapperUtils.mapIfNotNull(domain, d -> mapper.map(d, TransactionDTO.class));
    }

    public List<TransactionDTO> toDTOList(List<Transaction> domains) {
        return Objects.requireNonNullElse(domains, List.<Transaction>of()).stream().map(this::toDTO).toList();
    }
}
