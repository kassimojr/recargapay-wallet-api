package com.recargapay.wallet.adapter.controllers.v1;

import com.recargapay.wallet.core.ports.in.TransferFundsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.recargapay.wallet.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithAnonymousUser;

@WebMvcTest(WalletController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public TransferFundsUseCase transferFundsUseCase() {
            return org.mockito.Mockito.mock(TransferFundsUseCase.class);
        }
    }

    @Autowired
    private TransferFundsUseCase transferFundsUseCase;

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 para transferência bem-sucedida")
    void shouldReturn200OnSuccess() throws Exception {
        Mockito.doNothing().when(transferFundsUseCase)
                .transfer(any(UUID.class), any(UUID.class), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/wallets/transfer")
                .param("fromWalletId", UUID.randomUUID().toString())
                .param("toWalletId", UUID.randomUUID().toString())
                .param("amount", "10.00")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 400 para valor inválido")
    void shouldReturn400OnInvalidValue() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("O valor da transferência deve ser positivo"))
                .when(transferFundsUseCase)
                .transfer(any(UUID.class), any(UUID.class), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/wallets/transfer")
                .param("fromWalletId", UUID.randomUUID().toString())
                .param("toWalletId", UUID.randomUUID().toString())
                .param("amount", "-1.00")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 para carteira não encontrada")
    void shouldReturn404OnWalletNotFound() throws Exception {
        Mockito.doThrow(new com.recargapay.wallet.core.exceptions.WalletNotFoundException("Carteira não encontrada"))
                .when(transferFundsUseCase)
                .transfer(any(UUID.class), any(UUID.class), any(BigDecimal.class));

        mockMvc.perform(post("/api/v1/wallets/transfer")
                .param("fromWalletId", UUID.randomUUID().toString())
                .param("toWalletId", UUID.randomUUID().toString())
                .param("amount", "10.00")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Deve exigir autenticação para transferir")
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/wallets/transfer")
                .param("fromWalletId", UUID.randomUUID().toString())
                .param("toWalletId", UUID.randomUUID().toString())
                .param("amount", "10.00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
