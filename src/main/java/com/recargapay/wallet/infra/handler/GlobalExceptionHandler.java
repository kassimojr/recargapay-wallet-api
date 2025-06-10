package com.recargapay.wallet.infra.handler;

import com.recargapay.wallet.core.exceptions.DuplicatedResourceException;
import com.recargapay.wallet.core.exceptions.InsufficientBalanceException;
import com.recargapay.wallet.core.exceptions.InsufficientFundsException;
import com.recargapay.wallet.core.exceptions.InvalidDateFormatException;
import com.recargapay.wallet.core.exceptions.UserNotFoundException;
import com.recargapay.wallet.core.exceptions.WalletNotFoundException;
import com.recargapay.wallet.core.exceptions.WalletAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the API that implements RFC 7807 (Problem Details)
 * standard for consistent error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Base URI for error types
    private static final String ERROR_TYPE_BASE = "https://api.recargapay.com/errors/";
    private static final String CAUSE_PROPERTY = "cause";
    private static final String TIMESTAMP_PROPERTY = "timestamp";
    
    /**
     * Creates a ProblemDetail object with basic information filled in
     *
     * @param status HTTP status code for the response
     * @param title problem title
     * @param detail problem details
     * @param typeKey problem type identifier
     * @return configured ProblemDetail object
     */
    private ProblemDetail createProblem(HttpStatus status, String title, String detail, String typeKey) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_TYPE_BASE + typeKey));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }
    
    /**
     * Handles input validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage()
            ));
        
        log.warn("Validation error: {}", validationErrors);
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST, 
            "Invalid input data",
            "Input data validation error",
            "validation-error"
        );
        
        problem.setProperty("validationErrors", validationErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles wallet not found exception
     */
    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleWalletNotFound(WalletNotFoundException ex) {
        log.warn("Wallet not found: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.NOT_FOUND,
            "Wallet not found",
            ex.getMessage(),
            "wallet-not-found"
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles insufficient balance exception
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Insufficient balance",
            ex.getMessage(),
            "insufficient-balance"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles insufficient funds exception
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Insufficient funds: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Insufficient balance to complete the operation",
            ex.getMessage(),
            "insufficient-funds"
        );
        
        // Add extra details that may be useful for the client
        problem.setProperty("code", "INSUFFICIENT_FUNDS");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles user not found exception
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.NOT_FOUND,
            "User not found",
            ex.getMessage(),
            "user-not-found"
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Handles duplicated resource exception
     */
    @ExceptionHandler(DuplicatedResourceException.class)
    public ResponseEntity<ProblemDetail> handleDuplicatedResource(DuplicatedResourceException ex) {
        log.warn("Duplicated resource: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Resource already exists",
            ex.getMessage(),
            "resource-exists"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handles illegal argument exception
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Invalid parameter",
            ex.getMessage(),
            "invalid-parameter"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles illegal state exception
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Invalid operation state",
            ex.getMessage(),
            "invalid-state"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handles invalid date format exception
     */
    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDateFormat(InvalidDateFormatException ex) {
        log.warn("Invalid date format: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Invalid date format",
            ex.getMessage(),
            "invalid-date-format"
        );
        
        // Add extra details that may be useful for the client
        problem.setProperty("code", "INVALID_DATE_FORMAT");
        problem.setProperty("expectedFormats", new String[] {
            "ISO (2023-01-01T12:00:00Z)", 
            "Simple (2023-01-01 12:00:00)"
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles wallet already exists exception
     */
    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleWalletAlreadyExistsException(WalletAlreadyExistsException ex) {
        log.warn("Wallet already exists: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Wallet already exists",
            ex.getMessage(),
            "wallet-exists"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handles data integrity exceptions, including issues with version fields
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity error: {}", ex.getMessage(), ex);
        
        // Check if it's the specific uninitialized version value error
        if (ex.getMessage() != null && ex.getMessage().contains("uninitialized version value")) {
            ProblemDetail problem = createProblem(
                HttpStatus.CONFLICT,
                "Concurrency conflict",
                "The wallet data was modified by another process. Please try again.",
                "optimistic-lock"
            );
            
            problem.setProperty(CAUSE_PROPERTY, ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
        }
        
        // Other integrity errors
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Data integrity error",
            ex.getMessage(),
            "data-integrity"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Handles optimistic locking exceptions (outdated concurrent version)
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLocking(OptimisticLockingFailureException ex) {
        log.error("Optimistic locking error: {}", ex.getMessage(), ex);
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Concurrency conflict",
            "The data was modified by another process during the operation. Please try again.",
            "concurrent-modification"
        );
        
        problem.setProperty(CAUSE_PROPERTY, ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Handles generic unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ProblemDetail problem = createProblem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "An unexpected error occurred. Please try again later.",
            "server-error"
        );
        
        problem.setProperty("exceptionType", ex.getClass().getName());
        
        if (ex.getCause() != null) {
            problem.setProperty(CAUSE_PROPERTY, ex.getCause().getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
