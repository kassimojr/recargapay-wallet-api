package com.recargapay.wallet.infra.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletDatabaseHealthIndicatorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WalletDatabaseHealthIndicator walletDatabaseHealthIndicator;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(jdbcTemplate);
    }

    @Test
    void constructor_ShouldInitializeWithJdbcTemplate() {
        // Given & When
        WalletDatabaseHealthIndicator indicator = new WalletDatabaseHealthIndicator(jdbcTemplate);
        
        // Then
        assertNotNull(indicator);
    }

    @Test
    void health_WithSuccessfulDatabaseConnection_ShouldReturnHealthUp() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenReturn(5);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals(5, health.getDetails().get("walletCount"));
        assertFalse(health.getDetails().containsKey("error"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithZeroWallets_ShouldReturnHealthUpWithZeroCount() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenReturn(0);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals(0, health.getDetails().get("walletCount"));
        assertFalse(health.getDetails().containsKey("error"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithLargeWalletCount_ShouldReturnHealthUpWithCorrectCount() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenReturn(1000);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals(1000, health.getDetails().get("walletCount"));
        assertFalse(health.getDetails().containsKey("error"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithTestQueryReturningNonOne_ShouldReturnHealthDown() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(0);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Database test query failed", health.getDetails().get("error"));
        assertFalse(health.getDetails().containsKey("walletCount"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate, never()).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    @DisplayName("Should return health DOWN when test query returns null")
    void health_WithTestQueryReturningNull_ShouldReturnHealthDown() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(null);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Database test query failed", health.getDetails().get("error"));
    }

    @Test
    void health_WithDataAccessExceptionOnTestQuery_ShouldReturnHealthDown() {
        // Given
        DataAccessException testException = new DataAccessException("Connection failed") {};
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(testException);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Connection failed", health.getDetails().get("error"));
        assertFalse(health.getDetails().containsKey("walletCount"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate, never()).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithDataAccessExceptionOnWalletCountQuery_ShouldReturnHealthDown() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        DataAccessException testException = new DataAccessException("Table not found") {};
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenThrow(testException);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Table not found", health.getDetails().get("error"));
        assertFalse(health.getDetails().containsKey("walletCount"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithRuntimeExceptionOnTestQuery_ShouldReturnHealthDown() {
        // Given
        RuntimeException testException = new RuntimeException("Unexpected error");
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(testException);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Unexpected error", health.getDetails().get("error"));
        assertFalse(health.getDetails().containsKey("walletCount"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate, never()).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithRuntimeExceptionOnWalletCountQuery_ShouldReturnHealthDown() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        RuntimeException testException = new RuntimeException("Query timeout");
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenThrow(testException);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Query timeout", health.getDetails().get("error"));
        assertFalse(health.getDetails().containsKey("walletCount"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    @DisplayName("Should return health DOWN when exception has null message")
    void health_WithExceptionWithNullMessage_ShouldReturnHealthDownWithNullError() {
        // Arrange
        RuntimeException exceptionWithNullMessage = new RuntimeException((String) null);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(exceptionWithNullMessage);

        // Act
        Health health = walletDatabaseHealthIndicator.health();

        // Assert
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Unknown database error occurred", health.getDetails().get("error"));
    }

    @Test
    void health_ShouldAlwaysIncludeDatabaseDetail() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenReturn(10);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertTrue(health.getDetails().containsKey("database"));
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithSuccessfulConnection_ShouldNotIncludeErrorDetail() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenReturn(15);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.UP, health.getStatus());
        assertFalse(health.getDetails().containsKey("error"));
        assertTrue(health.getDetails().containsKey("walletCount"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    void health_WithException_ShouldNotIncludeWalletCountDetail() {
        // Given
        RuntimeException testException = new RuntimeException("Database error");
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenThrow(testException);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertTrue(health.getDetails().containsKey("error"));
        assertFalse(health.getDetails().containsKey("walletCount"));
        
        verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        verify(jdbcTemplate, never()).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }

    @Test
    @DisplayName("Should handle null wallet count gracefully")
    void health_WithNullWalletCount_ShouldHandleGracefully() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenReturn(null);
        
        // When
        Health health = walletDatabaseHealthIndicator.health();
        
        // Then
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("PostgreSQL", health.getDetails().get("database"));
        assertEquals("Wallet count query returned null", health.getDetails().get("error"));
    }

    @Test
    void health_ShouldExecuteQueriesInCorrectOrder() {
        // Given
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wallets", Integer.class)).thenReturn(20);
        
        // When
        walletDatabaseHealthIndicator.health();
        
        // Then - verify the order of calls
        var inOrder = inOrder(jdbcTemplate);
        inOrder.verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
        inOrder.verify(jdbcTemplate).queryForObject("SELECT COUNT(*) FROM wallets", Integer.class);
    }
}
