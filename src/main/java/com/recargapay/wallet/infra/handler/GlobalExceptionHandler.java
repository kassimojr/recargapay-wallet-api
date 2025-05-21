package com.recargapay.wallet.infra.handler;

import com.recargapay.wallet.core.exceptions.DuplicatedResourceException;
import com.recargapay.wallet.core.exceptions.InsufficientBalanceException;
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
 * Tratador global de exceções para a API que implementa o padrão RFC 7807 (Problem Details)
 * para respostas de erro consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Base URI para tipos de erro
    private static final String ERROR_TYPE_BASE = "https://api.recargapay.com/errors/";
    private static final String CAUSE_PROPERTY = "cause";
    private static final String TIMESTAMP_PROPERTY = "timestamp";
    
    /**
     * Cria um objeto ProblemDetail com as informações básicas preenchidas
     *
     * @param status código HTTP de status da resposta
     * @param title título do problema
     * @param detail detalhes do problema
     * @param typeKey identificador do tipo de problema
     * @return objeto ProblemDetail configurado
     */
    private ProblemDetail createProblem(HttpStatus status, String title, String detail, String typeKey) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(ERROR_TYPE_BASE + typeKey));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }
    
    /**
     * Manipula erros de validação de entrada
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() == null ? "Valor inválido" : fieldError.getDefaultMessage()
            ));
        
        log.warn("Erro de validação: {}", validationErrors);
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST, 
            "Dados de entrada inválidos",
            "Erro de validação dos dados de entrada",
            "validation-error"
        );
        
        problem.setProperty("validationErrors", validationErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Manipula exceção de carteira não encontrada
     */
    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleWalletNotFound(WalletNotFoundException ex) {
        log.warn("Carteira não encontrada: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.NOT_FOUND,
            "Carteira não encontrada",
            ex.getMessage(),
            "wallet-not-found"
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Manipula exceção de saldo insuficiente
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Saldo insuficiente: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Saldo insuficiente",
            ex.getMessage(),
            "insufficient-balance"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Manipula exceção de usuário não encontrado
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex) {
        log.warn("Usuário não encontrado: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.NOT_FOUND,
            "Usuário não encontrado",
            ex.getMessage(),
            "user-not-found"
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    /**
     * Manipula exceção de recurso duplicado
     */
    @ExceptionHandler(DuplicatedResourceException.class)
    public ResponseEntity<ProblemDetail> handleDuplicatedResource(DuplicatedResourceException ex) {
        log.warn("Recurso duplicado: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Recurso já existe",
            ex.getMessage(),
            "resource-exists"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Manipula exceção de argumento ilegal
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Parâmetro inválido",
            ex.getMessage(),
            "invalid-parameter"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Manipula exceção de estado ilegal
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Estado ilegal: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Estado de operação inválido",
            ex.getMessage(),
            "invalid-state"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Manipula exceção de carteira já existente
     */
    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleWalletAlreadyExistsException(WalletAlreadyExistsException ex) {
        log.warn("Carteira já existe: {}", ex.getMessage());
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Carteira já existe",
            ex.getMessage(),
            "wallet-exists"
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Manipula exceções de integridade de dados, incluindo problemas com campos de versão
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Erro de integridade de dados: {}", ex.getMessage(), ex);
        
        // Verifica se é o erro específico de versão não inicializada
        if (ex.getMessage() != null && ex.getMessage().contains("uninitialized version value")) {
            ProblemDetail problem = createProblem(
                HttpStatus.CONFLICT,
                "Conflito de concorrência",
                "Os dados da carteira foram modificados por outro processo. Por favor, tente novamente.",
                "optimistic-lock"
            );
            
            problem.setProperty(CAUSE_PROPERTY, ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
        }
        
        // Outros erros de integridade
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Erro de integridade de dados",
            ex.getMessage(),
            "data-integrity"
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Manipula exceções de bloqueio otimista (versão concorrente desatualizada)
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLocking(OptimisticLockingFailureException ex) {
        log.error("Erro de bloqueio otimista: {}", ex.getMessage(), ex);
        
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Conflito de concorrência",
            "Os dados foram alterados por outro processo durante a operação. Por favor, tente novamente.",
            "concurrent-modification"
        );
        
        problem.setProperty(CAUSE_PROPERTY, ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    /**
     * Manipula exceções genéricas não tratadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        
        ProblemDetail problem = createProblem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno do servidor",
            "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
            "server-error"
        );
        
        problem.setProperty("exceptionType", ex.getClass().getName());
        
        if (ex.getCause() != null) {
            problem.setProperty(CAUSE_PROPERTY, ex.getCause().getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
