package com.digital.wallet.infra.health;

import com.digital.wallet.core.ports.in.FindAllWalletsUseCase;
import com.digital.wallet.core.domain.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceHealthIndicatorTest {

    @Mock
    private FindAllWalletsUseCase findAllWalletsUseCase;

    @InjectMocks
    private WalletServiceHealthIndicator walletServiceHealthIndicator;

    private List<Wallet> mockWallets;

    @BeforeEach
    void setUp() {
        // Create mock wallets for testing
        Wallet wallet1 = new Wallet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00")
        );
        wallet1.setCreatedAt(LocalDateTime.now());
        wallet1.setUpdatedAt(LocalDateTime.now());
        
        Wallet wallet2 = new Wallet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.50")
        );
        wallet2.setCreatedAt(LocalDateTime.now());
        wallet2.setUpdatedAt(LocalDateTime.now());
        
        mockWallets = Arrays.asList(wallet1, wallet2);
    }

    @Test
    void constructor_ShouldInitializeWithFindAllWalletsUseCase() {
        // Given & When
        WalletServiceHealthIndicator indicator = new WalletServiceHealthIndicator(findAllWalletsUseCase);
        
        // Then
        assertNotNull(indicator);
    }

    @Test
    void health_WithSuccessfulWalletRetrieval_ShouldReturnHealthUp() {
        // Given
        when(findAllWalletsUseCase.findAll()).thenReturn(mockWallets);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals(2L, health.getDetails().get("walletCount"));
        assertNotNull(health.getDetails().get("responseTimeMs"));
        assertTrue((Long) health.getDetails().get("responseTimeMs") >= 0);
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithEmptyWalletList_ShouldReturnHealthUpWithZeroCount() {
        // Given
        when(findAllWalletsUseCase.findAll()).thenReturn(Collections.emptyList());
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals(0L, health.getDetails().get("walletCount"));
        assertNotNull(health.getDetails().get("responseTimeMs"));
        assertTrue((Long) health.getDetails().get("responseTimeMs") >= 0);
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithSingleWallet_ShouldReturnHealthUpWithCorrectCount() {
        // Given
        List<Wallet> singleWallet = Collections.singletonList(mockWallets.get(0));
        when(findAllWalletsUseCase.findAll()).thenReturn(singleWallet);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals(1L, health.getDetails().get("walletCount"));
        assertNotNull(health.getDetails().get("responseTimeMs"));
        assertTrue((Long) health.getDetails().get("responseTimeMs") >= 0);
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithLargeWalletList_ShouldReturnHealthUpWithCorrectCount() {
        // Given
        List<Wallet> largeWalletList = Arrays.asList(
                mockWallets.get(0), mockWallets.get(1), mockWallets.get(0), 
                mockWallets.get(1), mockWallets.get(0)
        );
        when(findAllWalletsUseCase.findAll()).thenReturn(largeWalletList);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals(5L, health.getDetails().get("walletCount"));
        assertNotNull(health.getDetails().get("responseTimeMs"));
        assertTrue((Long) health.getDetails().get("responseTimeMs") >= 0);
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithRuntimeException_ShouldReturnHealthDown() {
        // Given
        RuntimeException testException = new RuntimeException("Service unavailable");
        when(findAllWalletsUseCase.findAll()).thenThrow(testException);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals("Service unavailable", health.getDetails().get("error"));
        assertNull(health.getDetails().get("walletCount"));
        assertNull(health.getDetails().get("responseTimeMs"));
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithIllegalArgumentException_ShouldReturnHealthDown() {
        // Given
        IllegalArgumentException testException = new IllegalArgumentException("Invalid argument");
        when(findAllWalletsUseCase.findAll()).thenThrow(testException);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals("Invalid argument", health.getDetails().get("error"));
        assertNull(health.getDetails().get("walletCount"));
        assertNull(health.getDetails().get("responseTimeMs"));
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithNullPointerException_ShouldReturnHealthDown() {
        // Given
        NullPointerException testException = new NullPointerException("Null pointer error");
        when(findAllWalletsUseCase.findAll()).thenThrow(testException);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals("Null pointer error", health.getDetails().get("error"));
        assertNull(health.getDetails().get("walletCount"));
        assertNull(health.getDetails().get("responseTimeMs"));
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    @DisplayName("Should return health DOWN when exception has null message")
    void health_WithExceptionWithNullMessage_ShouldReturnHealthDownWithNullError() {
        // Arrange
        RuntimeException exceptionWithNullMessage = new RuntimeException((String) null);
        when(findAllWalletsUseCase.findAll()).thenThrow(exceptionWithNullMessage);

        // Act
        Health health = walletServiceHealthIndicator.health();

        // Assert
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Wallet", health.getDetails().get("service"));
        assertEquals("Unknown error occurred", health.getDetails().get("error"));
    }

    @Test
    void health_ShouldMeasureResponseTime() {
        // Given
        when(findAllWalletsUseCase.findAll()).thenAnswer(invocation -> {
            // Simulate some processing time
            Thread.sleep(10);
            return mockWallets;
        });
        
        // When
        long startTime = System.currentTimeMillis();
        Health health = walletServiceHealthIndicator.health();
        long endTime = System.currentTimeMillis();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        Long responseTime = (Long) health.getDetails().get("responseTimeMs");
        assertNotNull(responseTime);
        assertTrue(responseTime >= 0);
        assertTrue(responseTime <= (endTime - startTime + 100)); // Allow some tolerance
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_ShouldAlwaysIncludeServiceDetail() {
        // Given
        when(findAllWalletsUseCase.findAll()).thenReturn(mockWallets);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertTrue(health.getDetails().containsKey("service"));
        assertEquals("Wallet", health.getDetails().get("service"));
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithSuccessfulCall_ShouldNotIncludeErrorDetail() {
        // Given
        when(findAllWalletsUseCase.findAll()).thenReturn(mockWallets);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertFalse(health.getDetails().containsKey("error"));
        assertTrue(health.getDetails().containsKey("walletCount"));
        assertTrue(health.getDetails().containsKey("responseTimeMs"));
        
        verify(findAllWalletsUseCase).findAll();
    }

    @Test
    void health_WithException_ShouldNotIncludeSuccessDetails() {
        // Given
        RuntimeException testException = new RuntimeException("Test error");
        when(findAllWalletsUseCase.findAll()).thenThrow(testException);
        
        // When
        Health health = walletServiceHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
        assertFalse(health.getDetails().containsKey("walletCount"));
        assertFalse(health.getDetails().containsKey("responseTimeMs"));
        
        verify(findAllWalletsUseCase).findAll();
    }
}
