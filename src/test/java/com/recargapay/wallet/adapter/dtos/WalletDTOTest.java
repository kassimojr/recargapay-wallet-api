package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WalletDTOTest {
    @Test
    void gettersAndSetters() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String userName = "Test User";
        BigDecimal balance = new BigDecimal("99.99");

        WalletDTO dto = new WalletDTO(id, userId, userName, balance);
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(userName, dto.getUserName());
        assertEquals(balance, dto.getBalance());
    }

    @Test
    void builderShouldBuildCorrectly() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String userName = "Test User";
        BigDecimal balance = new BigDecimal("50.00");
        WalletDTO dto = WalletDTO.builder()
                .id(id)
                .userId(userId)
                .userName(userName)
                .balance(balance)
                .build();
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(userName, dto.getUserName());
        assertEquals(balance, dto.getBalance());
    }

    @Test
    void noArgsConstructorAndSetters() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String userName = "Test User";
        BigDecimal balance = new BigDecimal("10.00");
        WalletDTO dto = new WalletDTO();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setUserName(userName);
        dto.setBalance(balance);
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(userName, dto.getUserName());
        assertEquals(balance, dto.getBalance());
    }
}
