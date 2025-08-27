package com.digital.wallet.adapter.controllers.v1;

import com.digital.wallet.adapter.converters.TransactionMapper;
import com.digital.wallet.adapter.converters.WalletMapper;
import com.digital.wallet.adapter.dtos.TransactionDTO;
import com.digital.wallet.adapter.dtos.TransactionHistoryResponseDTO;
import com.digital.wallet.core.domain.Transaction;
import com.digital.wallet.core.domain.Wallet;
import com.digital.wallet.core.ports.in.CreateWalletUseCase;
import com.digital.wallet.core.ports.in.TransactionHistoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "API for transaction management")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final CreateWalletUseCase createWalletUseCase;
    private final TransactionHistoryUseCase transactionHistoryUseCase;
    private final TransactionMapper transactionMapper;
    private final WalletMapper walletMapper;

    public TransactionController(
            CreateWalletUseCase createWalletUseCase,
            TransactionHistoryUseCase transactionHistoryUseCase,
            TransactionMapper transactionMapper,
            WalletMapper walletMapper) {
        this.createWalletUseCase = createWalletUseCase;
        this.transactionHistoryUseCase = transactionHistoryUseCase;
        this.transactionMapper = transactionMapper;
        this.walletMapper = walletMapper;
    }

    @Operation(
        summary = "Get wallet transaction history", 
        description = "Returns the transaction history of a specific wallet, with date filter options",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Transaction history successfully returned",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionHistoryResponseDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid date parameters",
                content = @Content(
                    mediaType = "application/json"
                )
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "Wallet not found",
                content = @Content(
                    mediaType = "application/json"
                )
            )
        }
    )
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<TransactionHistoryResponseDTO> getWalletTransactions(
            @Parameter(description = "Wallet ID", required = true)
            @PathVariable UUID walletId,
            
            @Parameter(description = "Specific date to filter transactions (format: yyyy-MM-dd or yyyy-MM-dd HH:mm:ss)")
            @RequestParam(value = "date", required = false) String date,
            
            @Parameter(description = "Start date to filter transactions (format: yyyy-MM-dd or yyyy-MM-dd HH:mm:ss)")
            @RequestParam(value = "startDate", required = false) String startDate,
            
            @Parameter(description = "End date to filter transactions (format: yyyy-MM-dd or yyyy-MM-dd HH:mm:ss)")
            @RequestParam(value = "endDate", required = false) String endDate) {
        
        // Get the wallet to retrieve the current balance
        Wallet wallet = createWalletUseCase.findById(walletId);
        
        // Get transactions based on parameters (all filter logic is in the service)
        List<Transaction> transactions = transactionHistoryUseCase.getFilteredTransactionHistory(
            walletId, date, startDate, endDate);
        
        // Convert to DTOs
        List<TransactionDTO> transactionDTOs = transactionMapper.toDTOList(transactions);
        
        // Get the user name from the mapper
        String userName = walletMapper.getUserName(wallet.getUserId());
        
        // Create the response
        TransactionHistoryResponseDTO response = new TransactionHistoryResponseDTO(
            walletId, userName, wallet.getBalance(), transactionDTOs);
        
        return ResponseEntity.ok(response);
    }
}
