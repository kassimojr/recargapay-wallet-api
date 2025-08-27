package com.digital.wallet.adapter.controllers.v1;

import com.digital.wallet.adapter.converters.WalletMapper;
import com.digital.wallet.adapter.converters.TransactionMapper;
import com.digital.wallet.adapter.dtos.DepositRequestDTO;
import com.digital.wallet.core.ports.in.CreateWalletUseCase;
import com.digital.wallet.core.ports.in.DepositUseCase;
import com.digital.wallet.core.ports.in.FindAllWalletsUseCase;
import com.digital.wallet.core.ports.in.TransferFundsUseCase;
import com.digital.wallet.core.ports.in.WithdrawUseCase;
import com.digital.wallet.adapter.dtos.CreateWalletRequestDTO;
import com.digital.wallet.adapter.dtos.WalletDTO;
import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.exceptions.WalletAlreadyExistsException;
import com.digital.wallet.core.exceptions.WalletNotFoundException;
import com.digital.wallet.adapter.dtos.TransactionDTO;
import com.digital.wallet.adapter.dtos.WithdrawRequestDTO;
import com.digital.wallet.core.domain.Transaction;
import com.digital.wallet.infra.config.TestOpenTelemetryConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.digital.wallet.infra.config.TestSecurityConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(WalletController.class)
@Import({TestSecurityConfig.class, TestOpenTelemetryConfig.class})
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

    // --- WALLET CREATION TESTS ---

    @Test
    @WithMockUser
    @DisplayName("Should return 201 when wallet is created successfully")
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
    @DisplayName("Should return 400 when creating wallet with invalid payload")
    void shouldReturn400OnCreateWalletInvalidPayload() throws Exception {
        String json = "{}"; // missing userId
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 409 when creating wallet for user that already has one")
    void shouldReturn409OnCreateWalletForExistingUser() throws Exception {
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        Mockito.when(walletMapper.toDomain(any(CreateWalletRequestDTO.class))).thenReturn(wallet);
        Mockito.when(createWalletUseCase.create(any(Wallet.class)))
                .thenThrow(new WalletAlreadyExistsException("User already has a wallet"));
        String json = "{\"userId\":\"" + userId + "\"}";
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should require authentication to create wallet")
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
    @DisplayName("Should return 500 for unexpected error when creating wallet")
    void shouldReturn500OnUnexpectedError() throws Exception {
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        Mockito.when(walletMapper.toDomain(any(CreateWalletRequestDTO.class))).thenReturn(wallet);
        Mockito.when(createWalletUseCase.create(any(Wallet.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        String json = "{\"userId\":\"" + userId + "\"}";
        mockMvc.perform(post("/api/v1/wallets")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    // --- TRANSFER TESTS ---

    @Test
    @WithMockUser
    @DisplayName("Should return 200 for successful transfer")
    void shouldReturn200OnSuccess() throws Exception {
        Mockito.when(transferFundsUseCase
                .transfer(any(UUID.class), any(UUID.class), any(BigDecimal.class)))
                .thenReturn(List.of(new Transaction(), new Transaction()));

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
    @DisplayName("Should return 400 for invalid value")
    void shouldReturn400OnInvalidValue() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Transfer amount must be positive"))
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
    @DisplayName("Should return 404 for wallet not found")
    void shouldReturn404OnWalletNotFound() throws Exception {
        Mockito.doThrow(new WalletNotFoundException("Wallet not found"))
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
    @DisplayName("Should require authentication for transfer")
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
    @DisplayName("Should return 200 when getting current balance")
    void shouldReturn200OnGetBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.TEN);
        WalletDTO walletDTO = new WalletDTO(walletId, UUID.randomUUID(), "Test User", BigDecimal.TEN);
        Mockito.when(createWalletUseCase.findById(walletId)).thenReturn(wallet);
        Mockito.when(walletMapper.toDTO(any(Wallet.class))).thenReturn(walletDTO);
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
    }

    // --- CURRENT BALANCE TESTS ---
    @Test
    @WithMockUser
    @DisplayName("Should return 200 with balance when wallet exists")
    void shouldReturn200WithBalanceWhenWalletExists() throws Exception {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.TEN);
        WalletDTO walletDTO = new WalletDTO(walletId, UUID.randomUUID(), "Test User", BigDecimal.TEN);
        Mockito.when(createWalletUseCase.findById(walletId)).thenReturn(wallet);
        Mockito.when(walletMapper.toDTO(wallet)).thenReturn(walletDTO);
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when wallet not found on balance")
    void shouldReturn404WhenWalletNotFoundOnBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        Mockito.when(createWalletUseCase.findById(walletId)).thenThrow(new WalletNotFoundException("Wallet not found"));
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should require authentication for get balance")
    void shouldRequireAuthForGetBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/wallets/" + walletId + "/balance"))
                .andExpect(status().isUnauthorized());
    }

    // --- DEPOSIT TESTS ---
    @Test
    @WithMockUser
    @DisplayName("Should return 200 on deposit success")
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
    @DisplayName("Should return 404 on deposit wallet not found")
    void shouldReturn404OnDepositWalletNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        DepositRequestDTO dto = new DepositRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(depositUseCase.deposit(walletId, amount)).thenThrow(new WalletNotFoundException("Wallet not found"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/deposit")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 on deposit invalid amount")
    void shouldReturn400OnDepositInvalidAmount() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(-10);
        DepositRequestDTO dto = new DepositRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(depositUseCase.deposit(walletId, amount)).thenThrow(new IllegalArgumentException("Invalid amount"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/deposit")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should require authentication to deposit")
    void shouldRequireAuthenticationToDeposit() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/deposit")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // --- WITHDRAWAL TESTS ---
    @Test
    @WithMockUser
    @DisplayName("Should return 200 on withdrawal success")
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
    @DisplayName("Should return 404 on withdrawal wallet not found")
    void shouldReturn404OnWithdrawWalletNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(50);
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(withdrawUseCase.withdraw(walletId, amount)).thenThrow(new WalletNotFoundException("Wallet not found"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/withdraw")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 on withdrawal invalid amount")
    void shouldReturn400OnWithdrawInvalidAmount() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(-10);
        WithdrawRequestDTO dto = new WithdrawRequestDTO();
        dto.setWalletId(walletId);
        dto.setAmount(amount);
        Mockito.when(withdrawUseCase.withdraw(walletId, amount)).thenThrow(new IllegalArgumentException("Invalid amount"));
        String json = String.format("{\"walletId\":\"%s\",\"amount\":%s}", walletId, amount);
        mockMvc.perform(post("/api/v1/wallets/withdraw")
                .content(json)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should require authentication to withdraw")
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
        
        WalletDTO walletDTO1 = new WalletDTO(walletId1, userId1, "Test User", BigDecimal.valueOf(100));
        WalletDTO walletDTO2 = new WalletDTO(walletId2, userId2, "Test User", BigDecimal.valueOf(200));
        
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
