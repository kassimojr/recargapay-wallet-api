package com.recargapay.wallet.adapter.controllers.v1;

import com.recargapay.wallet.adapter.converters.WalletMapper;
import com.recargapay.wallet.adapter.converters.TransactionMapper;
import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.core.ports.in.CreateWalletUseCase;
import com.recargapay.wallet.core.ports.in.DepositUseCase;
import com.recargapay.wallet.core.ports.in.FindAllWalletsUseCase;
import com.recargapay.wallet.core.ports.in.TransferFundsUseCase;
import com.recargapay.wallet.core.ports.in.WithdrawUseCase;
import com.recargapay.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.recargapay.wallet.adapter.dtos.WalletDTO;
import com.recargapay.wallet.core.domain.Wallet;
import com.recargapay.wallet.core.exceptions.WalletAlreadyExistsException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.adapter.dtos.TransactionDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;
import com.recargapay.wallet.core.domain.Transaction;
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
import java.util.Arrays;
import java.util.List;
import org.springframework.security.test.context.support.WithAnonymousUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
        @Bean
        public FindAllWalletsUseCase findAllWalletsUseCase() {
            return mock(FindAllWalletsUseCase.class);
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

    @Autowired
    private WalletMapper walletMapper;
    
    @Autowired
    private FindAllWalletsUseCase findAllWalletsUseCase;

    // --- TESTES DE CRIAÇÃO DE CARTEIRA ---

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 201 ao criar carteira com sucesso")
    void shouldReturn201OnCreateWalletSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(UUID.randomUUID());
        walletDTO.setUserId(userId);
        walletDTO.setBalance(BigDecimal.ZERO);

        Mockito.when(walletMapper.toDomain(any(CreateWalletRequestDTO.class))).thenReturn(wallet);
        Mockito.when(createWalletUseCase.create(any(Wallet.class))).thenReturn(wallet);
        Mockito.when(walletMapper.toDTO(any(Wallet.class))).thenReturn(walletDTO);

        String json = "{\"userId\":\"" + userId + "\"}";
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 400 ao criar carteira com payload inválido")
    void shouldReturn400OnCreateWalletInvalidPayload() throws Exception {
        String json = "{}"; // userId ausente
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 409 ao criar carteira para usuário que já possui uma")
    void shouldReturn409OnCreateWalletForExistingUser() throws Exception {
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        Mockito.when(walletMapper.toDomain(any(CreateWalletRequestDTO.class))).thenReturn(wallet);
        Mockito.when(createWalletUseCase.create(any(Wallet.class)))
                .thenThrow(new WalletAlreadyExistsException("Usuário já possui carteira"));
        String json = "{\"userId\":\"" + userId + "\"}";
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Deve exigir autenticação para criar carteira")
    void shouldRequireAuthenticationToCreateWallet() throws Exception {
        UUID userId = UUID.randomUUID();
        String json = "{\"userId\":\"" + userId + "\"}";
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 500 para erro inesperado ao criar carteira")
    void shouldReturn500OnUnexpectedError() throws Exception {
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        Mockito.when(walletMapper.toDomain(any(CreateWalletRequestDTO.class))).thenReturn(wallet);
        Mockito.when(createWalletUseCase.create(any(Wallet.class)))
                .thenThrow(new RuntimeException("Erro inesperado"));
        String json = "{\"userId\":\"" + userId + "\"}";
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // --- TESTES DE TRANSFERÊNCIA ---

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
        Mockito.doThrow(new WalletNotFoundException("Carteira não encontrada"))
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

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 ao consultar saldo atual da carteira")
    void shouldReturn200OnGetBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.TEN);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(walletId);
        walletDTO.setBalance(BigDecimal.TEN);
        Mockito.when(createWalletUseCase.findById(walletId)).thenReturn(wallet);
        Mockito.when(walletMapper.toDTO(any(Wallet.class))).thenReturn(walletDTO);
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 ao consultar saldo histórico da carteira")
    void shouldReturn200OnGetHistoricalBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        String at = "2024-05-07T00:00:00Z";
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ONE);
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(walletId);
        walletDTO.setBalance(BigDecimal.ONE);
        Mockito.when(createWalletUseCase.findBalanceAt(walletId, at)).thenReturn(wallet);
        Mockito.when(walletMapper.toDTO(wallet)).thenReturn(walletDTO);
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance/history")
                .param("at", at)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    // --- TESTES DE SALDO ATUAL ---
    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 e saldo atual quando a carteira existe")
    void shouldReturn200WithBalanceWhenWalletExists() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.TEN);
        WalletDTO walletDTO = new WalletDTO(walletId, UUID.randomUUID(), BigDecimal.TEN);
        Mockito.when(createWalletUseCase.findById(walletId)).thenReturn(wallet);
        Mockito.when(walletMapper.toDTO(wallet)).thenReturn(walletDTO);
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 quando a carteira não existe ao consultar saldo atual")
    void shouldReturn404WhenWalletNotFoundOnBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        Mockito.when(createWalletUseCase.findById(walletId)).thenThrow(new WalletNotFoundException("Carteira não encontrada"));
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Deve exigir autenticação para consultar saldo atual")
    void shouldRequireAuthForGetBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance"))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTES DE SALDO HISTÓRICO ---
    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 e saldo histórico quando a carteira existe e data é válida")
    void shouldReturn200WithHistoricalBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        String at = "2024-01-01T00:00:00";
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.TEN);
        WalletDTO walletDTO = new WalletDTO(walletId, UUID.randomUUID(), BigDecimal.TEN);
        Mockito.when(createWalletUseCase.findBalanceAt(walletId, at)).thenReturn(wallet);
        Mockito.when(walletMapper.toDTO(wallet)).thenReturn(walletDTO);
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance/history?at=" + at))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 quando a carteira não existe ao consultar saldo histórico")
    void shouldReturn404WhenWalletNotFoundOnHistoricalBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        String at = "2024-01-01T00:00:00";
        Mockito.when(createWalletUseCase.findBalanceAt(walletId, at)).thenThrow(new WalletNotFoundException("Carteira não encontrada"));
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance/history?at=" + at))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Deve exigir autenticação para consultar saldo histórico")
    void shouldRequireAuthForGetHistoricalBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        String at = "2024-01-01T00:00:00";
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance/history?at=" + at))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTES DE DEPÓSITO ---
    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 ao depositar com sucesso")
    void shouldReturn200OnDepositSuccess() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        DepositRequestDTO dto = new DepositRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Transaction transaction = new Transaction();
        TransactionDTO transactionDTO = new TransactionDTO();
        Mockito.when(depositUseCase.deposit(walletId, amount)).thenReturn(transaction);
        Mockito.when(transactionMapper.toDTO(transaction)).thenReturn(transactionDTO);
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/deposit")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 ao tentar depositar em carteira inexistente")
    void shouldReturn404OnDepositWalletNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        DepositRequestDTO dto = new DepositRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(depositUseCase.deposit(walletId, amount)).thenThrow(new WalletNotFoundException("Carteira não encontrada"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/deposit")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 400 ao tentar depositar valor inválido")
    void shouldReturn400OnDepositInvalidAmount() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(-10);
        DepositRequestDTO dto = new DepositRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(depositUseCase.deposit(walletId, amount)).thenThrow(new IllegalArgumentException("Valor inválido"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/deposit")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Deve exigir autenticação para depositar")
    void shouldRequireAuthenticationToDeposit() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/deposit")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTES DE SAQUE ---
    @Test
    @WithMockUser
    @DisplayName("Deve retornar 200 ao sacar com sucesso")
    void shouldReturn200OnWithdrawSuccess() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(50);
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Transaction transaction = new Transaction();
        TransactionDTO transactionDTO = new TransactionDTO();
        Mockito.when(withdrawUseCase.withdraw(walletId, amount)).thenReturn(transaction);
        Mockito.when(transactionMapper.toDTO(transaction)).thenReturn(transactionDTO);
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/withdraw")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 404 ao tentar sacar de carteira inexistente")
    void shouldReturn404OnWithdrawWalletNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(50);
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(withdrawUseCase.withdraw(walletId, amount)).thenThrow(new WalletNotFoundException("Carteira não encontrada"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/withdraw")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar 400 ao tentar sacar valor inválido")
    void shouldReturn400OnWithdrawInvalidAmount() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(-10);
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(withdrawUseCase.withdraw(walletId, amount)).thenThrow(new IllegalArgumentException("Valor inválido"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/withdraw")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Deve exigir autenticação para sacar")
    void shouldRequireAuthenticationToWithdraw() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(50);
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/withdraw")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return all wallets")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnAllWallets() throws Exception {
        // Arrange
        UUID walletId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID userId1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Wallet wallet1 = new Wallet(walletId1, userId1, BigDecimal.valueOf(100));

        UUID walletId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID userId2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Wallet wallet2 = new Wallet(walletId2, userId2, BigDecimal.valueOf(200));

        List<Wallet> wallets = Arrays.asList(wallet1, wallet2);
        
        WalletDTO walletDTO1 = new WalletDTO();
        walletDTO1.setId(walletId1);
        walletDTO1.setUserId(userId1);
        walletDTO1.setBalance(BigDecimal.valueOf(100));
        
        WalletDTO walletDTO2 = new WalletDTO();
        walletDTO2.setId(walletId2);
        walletDTO2.setUserId(userId2);
        walletDTO2.setBalance(BigDecimal.valueOf(200));
        
        List<WalletDTO> walletDTOs = Arrays.asList(walletDTO1, walletDTO2);
        
        when(findAllWalletsUseCase.findAll()).thenReturn(wallets);
        when(walletMapper.toDTOList(wallets)).thenReturn(walletDTOs);
        
        // Act and Assert
        mockMvc.perform(get("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(walletId1.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId1.toString()))
                .andExpect(jsonPath("$[0].balance").value(100))
                .andExpect(jsonPath("$[1].id").value(walletId2.toString()))
                .andExpect(jsonPath("$[1].userId").value(userId2.toString()))
                .andExpect(jsonPath("$[1].balance").value(200));
    }
    
    @Test
    @DisplayName("Should return empty list when no wallets")
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnEmptyListWhenNoWallets() throws Exception {
        // Arrange
        when(findAllWalletsUseCase.findAll()).thenReturn(List.of());
        when(walletMapper.toDTOList(List.of())).thenReturn(List.of());
        
        // Act and Assert
        mockMvc.perform(get("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
    
    @Test
    @DisplayName("Should return 401 when not authenticated")
    @WithAnonymousUser
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
