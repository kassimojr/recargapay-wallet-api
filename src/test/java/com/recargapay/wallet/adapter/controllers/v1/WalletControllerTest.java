package com.recargapay.wallet.adapter.controllers.v1;

import com.recargapay.wallet.adapter.converters.WalletMapper;

import com.recargapay.wallet.adapter.converters.TransactionMapper;
import com.recargapay.wallet.core.ports.in.CreateWalletUseCase;
import com.recargapay.wallet.core.ports.in.DepositUseCase;
import com.recargapay.wallet.core.ports.in.TransferFundsUseCase;
import com.recargapay.wallet.core.ports.in.WithdrawUseCase;
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
import static org.mockito.Mockito.mock;
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
            return mock(TransferFundsUseCase.class);
        }
        @Bean
        public CreateWalletUseCase createWalletUseCase() {
            return mock(CreateWalletUseCase.class);
        }
        @Bean
        public DepositUseCase depositUseCase() {
            return mock(DepositUseCase.class);
        }
        @Bean
        public WithdrawUseCase withdrawUseCase() {
            return mock(WithdrawUseCase.class);
        }
        @Bean
        public TransactionMapper transactionMapper() {
            return mock(TransactionMapper.class);
        }
        @Bean
        public WalletMapper walletMapper() {
            return mock(WalletMapper.class);
        }
    }

    @Autowired
    private TransferFundsUseCase transferFundsUseCase;

    @Autowired
    private CreateWalletUseCase createWalletUseCase;

    @Autowired
    private DepositUseCase depositUseCase;

    @Autowired
    private WithdrawUseCase withdrawUseCase;

    @Autowired
    private TransactionMapper transactionMapper;

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 para transferência bem-sucedida")
    void shouldReturn200OnSuccess() throws Exception {
        Mockito.doNothing().when(transferFundsUseCase)
                .transfer(any(UUID.class), any(UUID.class), any(BigDecimal.class));

        String fromWalletId = UUID.randomUUID().toString();
        String toWalletId = UUID.randomUUID().toString();
        String json = """
            {\n  \"fromWalletId\": \"%s\",\n  \"toWalletId\": \"%s\",\n  \"amount\": 10.00\n}""".formatted(fromWalletId, toWalletId);
        mockMvc.perform(post("/api/v1/wallets/transfer")
                .content(json)
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

        String fromWalletId = UUID.randomUUID().toString();
        String toWalletId = UUID.randomUUID().toString();
        String json = """
            {\n  \"fromWalletId\": \"%s\",\n  \"toWalletId\": \"%s\",\n  \"amount\": -1.00\n}""".formatted(fromWalletId, toWalletId);
        mockMvc.perform(post("/api/v1/wallets/transfer")
                .content(json)
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

        String fromWalletId = UUID.randomUUID().toString();
        String toWalletId = UUID.randomUUID().toString();
        String json = """
            {\n  \"fromWalletId\": \"%s\",\n  \"toWalletId\": \"%s\",\n  \"amount\": 10.00\n}""".formatted(fromWalletId, toWalletId);
        mockMvc.perform(post("/api/v1/wallets/transfer")
                .content(json)
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
