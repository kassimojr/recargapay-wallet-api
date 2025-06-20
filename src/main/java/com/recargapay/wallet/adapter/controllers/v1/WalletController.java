package com.recargapay.wallet.adapter.controllers.v1;

import com.recargapay.wallet.adapter.dtos.*;
import com.recargapay.wallet.core.domain.Transaction;
import com.recargapay.wallet.core.ports.in.CreateWalletUseCase;
import com.recargapay.wallet.core.ports.in.DepositUseCase;
import com.recargapay.wallet.core.ports.in.FindAllWalletsUseCase;
import com.recargapay.wallet.core.ports.in.TransferFundsUseCase;
import com.recargapay.wallet.core.ports.in.WithdrawUseCase;
import com.recargapay.wallet.adapter.converters.WalletMapper;
import com.recargapay.wallet.adapter.converters.TransactionMapper;
import com.recargapay.wallet.core.domain.Wallet;
import java.util.UUID;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.TransferRequestDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;
import com.recargapay.wallet.adapter.dtos.TransactionDTO;

@RestController
@RequestMapping("/api/v1/wallets")
@Tag(name = "Wallets", description = "API for wallet management")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    private final TransferFundsUseCase transferFundsUseCase;
    private final CreateWalletUseCase createWalletUseCase;
    private final WalletMapper walletMapper;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransactionMapper transactionMapper;
    private final FindAllWalletsUseCase findAllWalletsUseCase;

    public WalletController(TransferFundsUseCase transferFundsUseCase, 
            CreateWalletUseCase createWalletUseCase, 
            DepositUseCase depositUseCase, 
            WithdrawUseCase withdrawUseCase, 
            WalletMapper walletMapper,
            TransactionMapper transactionMapper,
            FindAllWalletsUseCase findAllWalletsUseCase) {
        this.transferFundsUseCase = transferFundsUseCase;
        this.createWalletUseCase = createWalletUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.walletMapper = walletMapper;
        this.transactionMapper = transactionMapper;
        this.findAllWalletsUseCase = findAllWalletsUseCase;
    }

    @Operation(
        summary = "List all wallets", 
        description = "Returns a list of all wallets registered in the system",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Wallet list successfully returned",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WalletDTO.class)
                )
            )
        }
    )
    @GetMapping
    public ResponseEntity<List<WalletDTO>> findAll() {
        List<Wallet> wallets = findAllWalletsUseCase.findAll();
        return ResponseEntity.ok(walletMapper.toDTOList(wallets));
    }

    @Operation(
        summary = "Get current wallet balance", 
        description = "Returns the details and current balance of a specific wallet",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Current balance successfully returned",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WalletDTO.class)
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
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<WalletDTO> getBalance(
            @Parameter(description = "Wallet ID", required = true)
            @PathVariable UUID walletId) {
        Wallet wallet = createWalletUseCase.findById(walletId);
        return ResponseEntity.ok(walletMapper.toDTO(wallet));
    }

    @Operation(
        summary = "Transfer funds between wallets", 
        description = "Transfers a specific amount from a source wallet to a destination wallet",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Transfer successfully completed",
                content = @Content(
                    mediaType = "application/json"
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Validation error or insufficient balance",
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
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @Parameter(description = "Transfer data", required = true)
            @Valid @RequestBody TransferRequestDTO request) {
        transferFundsUseCase.transfer(request.getFromWalletId(), request.getToWalletId(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Create new wallet", 
        description = "Creates a new wallet associated with a user",
        responses = {
            @ApiResponse(
                responseCode = "201", 
                description = "Wallet successfully created",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = WalletDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Validation error",
                content = @Content(
                    mediaType = "application/json"
                )
            )
        }
    )
    @PostMapping
    public ResponseEntity<WalletDTO> create(
            @Parameter(description = "New wallet data", required = true)
            @Valid @RequestBody CreateWalletRequestDTO dto) {
        Wallet wallet = walletMapper.toDomain(dto);
        Wallet created = createWalletUseCase.create(wallet);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletMapper.toDTO(created));
    }

    @Operation(
        summary = "Deposit to wallet", 
        description = "Makes a deposit to a specific wallet",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Deposit successfully completed",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Validation error or wallet not found",
                content = @Content(
                    mediaType = "application/json"
                )
            )
        }
    )
    @PostMapping("/deposit")
    public ResponseEntity<TransactionDTO> deposit(
            @Parameter(description = "Deposit data", required = true)
            @Valid @RequestBody DepositRequestDTO dto) {
        Transaction transaction = depositUseCase.deposit(dto.getWalletId(), dto.getAmount());
        return ResponseEntity.ok(transactionMapper.toDTO(transaction));
    }

    @Operation(
        summary = "Withdraw from wallet", 
        description = "Makes a withdrawal from a specific wallet",
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Withdrawal successfully completed",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransactionDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Validation error, wallet not found or insufficient balance",
                content = @Content(
                    mediaType = "application/json"
                )
            )
        }
    )
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDTO> withdraw(
            @Parameter(description = "Withdrawal data", required = true)
            @Valid @RequestBody WithdrawRequestDTO dto) {
        Transaction transaction = withdrawUseCase.withdraw(dto.getWalletId(), dto.getAmount());
        return ResponseEntity.ok(transactionMapper.toDTO(transaction));
    }
}
