package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {
    @Test
    void gettersAndSetters() {
        UUID id = UUID.randomUUID();
        String email = "user@email.com";
        String name = "Test User";

        UserDTO dto = new UserDTO(id, email, name);
        assertEquals(id, dto.getId());
        assertEquals(email, dto.getEmail());
        assertEquals(name, dto.getName());
    }
}
