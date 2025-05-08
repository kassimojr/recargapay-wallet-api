package com.recargapay.wallet.adapter.converters;

import com.recargapay.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;
import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.core.domain.Wallet;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WalletMapperTest {
    private final WalletMapper mapper = new WalletMapper(new ModelMapper());

    @Test
    void toDomainFromCreateWalletRequestDTO_shouldMapFields() {
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO();
        UUID userId = UUID.randomUUID();
        dto.setUserId(userId);
        Wallet wallet = mapper.toDomain(dto);
        assertNotNull(wallet);
        assertEquals(userId, wallet.getUserId());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
    }

    @Test
    void toDomainFromDepositRequestDTO_shouldMapFields() {
        DepositRequestDTO dto = new DepositRequestDTO();
        UUID walletId = UUID.randomUUID();
        dto.setWalletId(walletId);
        Wallet wallet = mapper.toDomain(dto);
        assertNotNull(wallet);
        assertEquals(walletId, wallet.getId());
    }

    @Test
    void toDomainFromWithdrawRequestDTO_shouldMapFields() {
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        UUID walletId = UUID.randomUUID();
        dto.setWalletId(walletId);
        Wallet wallet = mapper.toDomain(dto);
        assertNotNull(wallet);
        assertEquals(walletId, wallet.getId());
    }

    @Test
    void toDTO_shouldMapFields() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.TEN;
        Wallet wallet = new Wallet(id, userId, balance);
        WalletDTO dto = mapper.toDTO(wallet);
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(balance, dto.getBalance());
    }

    @Test
    void toDTO_shouldReturnNullIfDomainIsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDomain_shouldReturnNullIfDtoIsNull() {
        assertNull(mapper.toDomain((CreateWalletRequestDTO) null));
        assertNull(mapper.toDomain((DepositRequestDTO) null));
        assertNull(mapper.toDomain((WithdrawRequestDTO) null));
    }

    @Test
    void toDTOList_shouldMapList() {
        Wallet wallet1 = new Wallet(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE);
        Wallet wallet2 = new Wallet(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN);
        List<WalletDTO> dtos = mapper.toDTOList(List.of(wallet1, wallet2));
        assertEquals(2, dtos.size());
    }

    @Test
    void toDTOList_shouldReturnEmptyListIfNull() {
        List<WalletDTO> dtos = mapper.toDTOList(null);
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
    }
}
