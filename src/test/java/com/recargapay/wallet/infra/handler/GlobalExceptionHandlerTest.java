package com.recargapay.wallet.infra.handler;

import com.recargapay.wallet.core.exceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should return BAD_REQUEST for MethodArgumentNotValidException")
    void testHandleValidationException() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        FieldError error = mock(FieldError.class);
        when(error.getField()).thenReturn("name");
        when(error.getDefaultMessage()).thenReturn("cannot be empty");
        fieldErrors.add(error);
        
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleValidationException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Invalid input data", problem.getTitle());
        assertEquals("Input data validation error", problem.getDetail());
        
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) problem.getProperties().get("validationErrors");
        assertNotNull(validationErrors);
        assertEquals("cannot be empty", validationErrors.get("name"));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleIllegalArgumentException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Invalid parameter", problem.getTitle());
        assertEquals("Invalid parameter", problem.getDetail());
    }

    @Test
    @DisplayName("Should return CONFLICT for IllegalStateException")
    void testHandleIllegalStateException() {
        // Arrange
        IllegalStateException ex = new IllegalStateException("Invalid state");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleIllegalStateException(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Invalid operation state", problem.getTitle());
        assertEquals("Invalid state", problem.getDetail());
    }

    @Test
    @DisplayName("Should return CONFLICT for WalletAlreadyExistsException")
    void testHandleWalletAlreadyExistsException() {
        // Arrange
        WalletAlreadyExistsException ex = new WalletAlreadyExistsException("Wallet already exists");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleWalletAlreadyExistsException(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Wallet already exists", problem.getTitle());
        assertEquals("Wallet already exists", problem.getDetail());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for InsufficientBalanceException")
    void testHandleInsufficientBalance() {
        // Arrange
        InsufficientBalanceException ex = new InsufficientBalanceException("Insufficient balance");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleInsufficientBalance(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Insufficient balance", problem.getTitle());
        assertEquals("Insufficient balance", problem.getDetail());
    }

    @Test
    @DisplayName("Should return NOT_FOUND for WalletNotFoundException")
    void testHandleWalletNotFound() {
        // Arrange
        WalletNotFoundException ex = new WalletNotFoundException("Wallet not found");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleWalletNotFound(ex);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Wallet not found", problem.getTitle());
        assertEquals("Wallet not found", problem.getDetail());
    }

    @Test
    @DisplayName("Should return NOT_FOUND for UserNotFoundException")
    void testHandleUserNotFound() {
        // Arrange
        UserNotFoundException ex = new UserNotFoundException("User not found");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleUserNotFound(ex);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("User not found", problem.getTitle());
        assertEquals("User not found", problem.getDetail());
    }

    @Test
    @DisplayName("Should return INTERNAL_SERVER_ERROR for generic Exception")
    void testHandleGenericException() {
        // Arrange
        Exception ex = new Exception("Internal error");
        WebRequest request = mock(WebRequest.class);
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleGenericException(ex, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Internal server error", problem.getTitle());
        assertEquals("An unexpected error occurred. Please try again later.", problem.getDetail());
        assertEquals(Exception.class.getName(), problem.getProperties().get("exceptionType"));
    }
    
    @Test
    @DisplayName("Should return CONFLICT when DataIntegrityViolationException contains uninitialized version error")
    void testHandleDataIntegrityViolation_WithVersionError() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "Detached entity with generated id '123' has an uninitialized version value 'null': com.example.Entity");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Concurrency conflict", problem.getTitle());
        assertTrue(problem.getDetail().contains("The wallet data has been modified"));
        assertNotNull(problem.getProperties().get("timestamp"));
    }
    
    @Test
    @DisplayName("Should return BAD_REQUEST for other types of DataIntegrityViolationException")
    void testHandleDataIntegrityViolation_WithOtherError() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Database constraint violation");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrityViolation(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Data integrity violation", problem.getTitle());
        assertEquals("Database constraint violation", problem.getDetail());
    }
    
    @Test
    @DisplayName("Should return CONFLICT for OptimisticLockingFailureException")
    void testHandleOptimisticLocking() {
        // Arrange
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException(
            "Another user has modified the data");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleOptimisticLocking(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Concurrency conflict", problem.getTitle());
        assertTrue(problem.getDetail().contains("Another user has modified the data"));
        assertNotNull(problem.getProperties().get("cause"));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for InsufficientFundsException")
    void testHandleInsufficientFunds() {
        // Arrange
        InsufficientFundsException ex = new InsufficientFundsException("Insufficient funds for transfer");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleInsufficientFunds(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Insufficient balance to complete the operation", problem.getTitle());
        assertEquals("Insufficient funds for transfer", problem.getDetail());
        assertEquals("INSUFFICIENT_FUNDS", problem.getProperties().get("code"));
    }

    @Test
    @DisplayName("Should return CONFLICT for DuplicatedResourceException")
    void testHandleDuplicatedResource() {
        // Arrange
        DuplicatedResourceException ex = new DuplicatedResourceException("Resource already exists");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleDuplicatedResource(ex);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Resource already exists", problem.getTitle());
        assertEquals("Resource already exists", problem.getDetail());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for InvalidDateFormatException")
    void testHandleInvalidDateFormat() {
        // Arrange
        InvalidDateFormatException ex = new InvalidDateFormatException("Invalid date format provided");
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleInvalidDateFormat(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        assertEquals("Invalid date format", problem.getTitle());
        assertEquals("Invalid date format provided", problem.getDetail());
        assertEquals("INVALID_DATE_FORMAT", problem.getProperties().get("code"));
        assertNotNull(problem.getProperties().get("expectedFormats"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with null default message")
    void testHandleValidationExceptionWithNullMessage() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        FieldError error = mock(FieldError.class);
        when(error.getField()).thenReturn("email");
        when(error.getDefaultMessage()).thenReturn(null); // Test null message handling
        fieldErrors.add(error);
        
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        
        // Act
        ResponseEntity<ProblemDetail> response = handler.handleValidationException(ex);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetail problem = response.getBody();
        assertNotNull(problem);
        
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) problem.getProperties().get("validationErrors");
        assertNotNull(validationErrors);
        assertEquals("Invalid value", validationErrors.get("email")); // Should use default message
    }

    @Test
    @DisplayName("Should handle createProblem method with all parameters")
    void testCreateProblemMethod() {
        // This test ensures the private createProblem method is covered through public methods
        // Testing different combinations to ensure all branches are covered
        
        // Test with WalletNotFoundException (different type)
        WalletNotFoundException ex1 = new WalletNotFoundException("Test wallet not found");
        ResponseEntity<ProblemDetail> response1 = handler.handleWalletNotFound(ex1);
        
        ProblemDetail problem1 = response1.getBody();
        assertNotNull(problem1);
        assertNotNull(problem1.getType());
        assertNotNull(problem1.getProperties().get("timestamp"));
        
        // Test with IllegalArgumentException (different type)
        IllegalArgumentException ex2 = new IllegalArgumentException("Test illegal argument");
        ResponseEntity<ProblemDetail> response2 = handler.handleIllegalArgumentException(ex2);
        
        ProblemDetail problem2 = response2.getBody();
        assertNotNull(problem2);
        assertNotNull(problem2.getType());
        assertNotNull(problem2.getProperties().get("timestamp"));
    }
}
