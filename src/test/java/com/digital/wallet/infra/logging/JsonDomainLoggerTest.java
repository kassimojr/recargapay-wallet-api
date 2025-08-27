package com.digital.wallet.infra.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonDomainLoggerTest {

    @Mock
    private Logger mockLogger;

    private JsonDomainLogger jsonDomainLogger;

    @BeforeEach
    void setUp() {
        // Mock LoggerFactory to return our mock logger
        try (MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(any(Class.class)))
                    .thenReturn(mockLogger);
            
            jsonDomainLogger = new JsonDomainLogger(JsonDomainLoggerTest.class);
        }
    }

    @Test
    void constructor_ShouldCreateLoggerWithCorrectClass() {
        // Given
        Class<?> testClass = String.class;
        
        // When
        try (MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(testClass))
                    .thenReturn(mockLogger);
            
            JsonDomainLogger logger = new JsonDomainLogger(testClass);
            
            // Then
            assertNotNull(logger);
            loggerFactoryMock.verify(() -> LoggerFactory.getLogger(testClass));
        }
    }

    @Test
    void logOperationStart_ShouldCallLoggingUtilsWithCorrectParameters() {
        // Given
        String operation = "DEPOSIT";
        String walletId = "wallet-123";
        String amount = "100.00";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logOperationStart(operation, walletId, amount);
            
            // Then
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    eq("status"), eq("START"),
                    eq("walletId"), eq(walletId),
                    eq("amount"), eq(amount)
            ));
        }
    }

    @Test
    void logOperationStart_WithNullValues_ShouldHandleGracefully() {
        // Given
        String operation = "WITHDRAW";
        String walletId = null;
        String amount = null;
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logOperationStart(operation, walletId, amount);
            
            // Then
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    eq("status"), eq("START"),
                    eq("walletId"), isNull(),
                    eq("amount"), isNull()
            ));
        }
    }

    @Test
    void logTransferStart_ShouldCallLoggingUtilsWithMapData() {
        // Given
        String operation = "TRANSFER";
        String fromWalletId = "wallet-from-123";
        String toWalletId = "wallet-to-456";
        String amount = "250.00";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logTransferStart(operation, fromWalletId, toWalletId, amount);
            
            // Then
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    mapCaptor.capture()
            ));
            
            Map<String, Object> capturedMap = mapCaptor.getValue();
            assertEquals("START", capturedMap.get("status"));
            assertEquals(fromWalletId, capturedMap.get("fromWalletId"));
            assertEquals(toWalletId, capturedMap.get("toWalletId"));
            assertEquals(amount, capturedMap.get("amount"));
            assertEquals("BRL", capturedMap.get("currency"));
        }
    }

    @Test
    void logTransferStart_WithNullValues_ShouldIncludeNullsInMap() {
        // Given
        String operation = "TRANSFER";
        String fromWalletId = null;
        String toWalletId = null;
        String amount = null;
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logTransferStart(operation, fromWalletId, toWalletId, amount);
            
            // Then
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    mapCaptor.capture()
            ));
            
            Map<String, Object> capturedMap = mapCaptor.getValue();
            assertEquals("START", capturedMap.get("status"));
            assertNull(capturedMap.get("fromWalletId"));
            assertNull(capturedMap.get("toWalletId"));
            assertNull(capturedMap.get("amount"));
            assertEquals("BRL", capturedMap.get("currency"));
        }
    }

    @Test
    void logOperationSuccess_ShouldCallLoggingUtilsWithMapData() {
        // Given
        String operation = "DEPOSIT";
        String walletId = "wallet-789";
        String amount = "150.00";
        String transactionId = "txn-abc123";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logOperationSuccess(operation, walletId, amount, transactionId);
            
            // Then
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    mapCaptor.capture()
            ));
            
            Map<String, Object> capturedMap = mapCaptor.getValue();
            assertEquals("SUCCESS", capturedMap.get("status"));
            assertEquals(walletId, capturedMap.get("walletId"));
            assertEquals(amount, capturedMap.get("amount"));
            assertEquals(transactionId, capturedMap.get("transactionId"));
            assertEquals("BRL", capturedMap.get("currency"));
        }
    }

    @Test
    void logOperationSuccess_WithEmptyStrings_ShouldIncludeEmptyStringsInMap() {
        // Given
        String operation = "WITHDRAW";
        String walletId = "";
        String amount = "";
        String transactionId = "";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logOperationSuccess(operation, walletId, amount, transactionId);
            
            // Then
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    mapCaptor.capture()
            ));
            
            Map<String, Object> capturedMap = mapCaptor.getValue();
            assertEquals("SUCCESS", capturedMap.get("status"));
            assertEquals("", capturedMap.get("walletId"));
            assertEquals("", capturedMap.get("amount"));
            assertEquals("", capturedMap.get("transactionId"));
            assertEquals("BRL", capturedMap.get("currency"));
        }
    }

    @Test
    void logTransferSuccess_ShouldCallLoggingUtilsWithCompleteMapData() {
        // Given
        String operation = "TRANSFER";
        String fromWalletId = "wallet-from-999";
        String toWalletId = "wallet-to-888";
        String amount = "500.00";
        String transactionId = "txn-transfer-xyz";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logTransferSuccess(operation, fromWalletId, toWalletId, amount, transactionId);
            
            // Then
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    mapCaptor.capture()
            ));
            
            Map<String, Object> capturedMap = mapCaptor.getValue();
            assertEquals("SUCCESS", capturedMap.get("status"));
            assertEquals(fromWalletId, capturedMap.get("fromWalletId"));
            assertEquals(toWalletId, capturedMap.get("toWalletId"));
            assertEquals(amount, capturedMap.get("amount"));
            assertEquals(transactionId, capturedMap.get("transactionId"));
            assertEquals("BRL", capturedMap.get("currency"));
        }
    }

    @Test
    void logTransferSuccess_WithSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        String operation = "TRANSFER_SPECIAL";
        String fromWalletId = "wallet-from-@#$";
        String toWalletId = "wallet-to-!%&";
        String amount = "1,234.56";
        String transactionId = "txn-special-chars-@#$%";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logTransferSuccess(operation, fromWalletId, toWalletId, amount, transactionId);
            
            // Then
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    mapCaptor.capture()
            ));
            
            Map<String, Object> capturedMap = mapCaptor.getValue();
            assertEquals("SUCCESS", capturedMap.get("status"));
            assertEquals(fromWalletId, capturedMap.get("fromWalletId"));
            assertEquals(toWalletId, capturedMap.get("toWalletId"));
            assertEquals(amount, capturedMap.get("amount"));
            assertEquals(transactionId, capturedMap.get("transactionId"));
            assertEquals("BRL", capturedMap.get("currency"));
        }
    }

    @Test
    void logOperationError_ShouldCallLoggingUtilsWithCorrectParameters() {
        // Given
        String operation = "DEPOSIT";
        String walletId = "wallet-error-123";
        String errorType = "INSUFFICIENT_FUNDS";
        String errorMessage = "Not enough balance for operation";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logOperationError(operation, walletId, errorType, errorMessage);
            
            // Then
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    eq("status"), eq("ERROR"),
                    eq("walletId"), eq(walletId),
                    eq("errorType"), eq(errorType)
            ));
        }
    }

    @Test
    void logOperationError_WithNullErrorType_ShouldHandleGracefully() {
        // Given
        String operation = "WITHDRAW";
        String walletId = "wallet-error-456";
        String errorType = null;
        String errorMessage = "Unknown error occurred";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logOperationError(operation, walletId, errorType, errorMessage);
            
            // Then
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    eq("status"), eq("ERROR"),
                    eq("walletId"), eq(walletId),
                    eq("errorType"), isNull()
            ));
        }
    }

    @Test
    void allMethods_ShouldUseCorrectLogger() {
        // Given
        String operation = "TEST_OPERATION";
        String walletId = "test-wallet";
        String amount = "100.00";
        String transactionId = "test-txn";
        String errorType = "TEST_ERROR";
        String errorMessage = "Test error message";
        
        // When & Then - All methods should use the same logger instance
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logOperationStart(operation, walletId, amount);
            jsonDomainLogger.logTransferStart(operation, walletId, walletId, amount);
            jsonDomainLogger.logOperationSuccess(operation, walletId, amount, transactionId);
            jsonDomainLogger.logTransferSuccess(operation, walletId, walletId, amount, transactionId);
            jsonDomainLogger.logOperationError(operation, walletId, errorType, errorMessage);
            
            // Verify all calls used the same logger instance
            loggingUtilsMock.verify(() -> LoggingUtils.log(eq(mockLogger), anyString(), any()), times(3));
            loggingUtilsMock.verify(() -> LoggingUtils.log(eq(mockLogger), anyString(), anyString(), any(), anyString(), any(), anyString(), any()), times(2));
        }
    }

    @Test
    void mapDataMethods_ShouldCreateLinkedHashMapWithCorrectOrder() {
        // Given
        String operation = "ORDER_TEST";
        String fromWalletId = "from-wallet";
        String toWalletId = "to-wallet";
        String amount = "100.00";
        String transactionId = "order-txn";
        
        // When
        try (MockedStatic<LoggingUtils> loggingUtilsMock = mockStatic(LoggingUtils.class)) {
            jsonDomainLogger.logTransferStart(operation, fromWalletId, toWalletId, amount);
            
            // Then
            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            loggingUtilsMock.verify(() -> LoggingUtils.log(
                    eq(mockLogger),
                    eq(operation),
                    mapCaptor.capture()
            ));
            
            Map<String, Object> capturedMap = mapCaptor.getValue();
            
            // Verify it's a LinkedHashMap (maintains insertion order)
            assertTrue(capturedMap instanceof java.util.LinkedHashMap);
            
            // Verify the order of keys
            String[] expectedOrder = {"status", "fromWalletId", "toWalletId", "amount", "currency"};
            String[] actualOrder = capturedMap.keySet().toArray(new String[0]);
            assertArrayEquals(expectedOrder, actualOrder);
        }
    }
}
