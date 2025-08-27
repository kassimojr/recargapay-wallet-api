package com.digital.wallet.adapter.dtos;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CreateWalletRequestDTOTest {
    @Test
    void gettersAndSetters() {
        UUID userId = UUID.randomUUID();
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO(userId);
        assertEquals(userId, dto.getUserId());
    }

    @Test
    void builderShouldBuildCorrectly() {
        UUID userId = UUID.randomUUID();
        CreateWalletRequestDTO dto = CreateWalletRequestDTO.builder()
                .userId(userId)
                .build();
        assertEquals(userId, dto.getUserId());
    }

    @Test
    void noArgsConstructorAndSetters() {
        UUID userId = UUID.randomUUID();
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO();
        dto.setUserId(userId);
        assertEquals(userId, dto.getUserId());
    }
}
