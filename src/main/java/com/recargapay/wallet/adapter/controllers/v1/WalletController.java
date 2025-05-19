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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.recargapay.wallet.adapter.dtos.DepositRequestDTO;
import com.recargapay.wallet.adapter.dtos.TransferRequestDTO;
import com.recargapay.wallet.adapter.dtos.WithdrawRequestDTO;
import com.recargapay.wallet.adapter.dtos.TransactionDTO;

@RestController
@RequestMapping("/api/v1/wallets")
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

    @Operation(summary = "Listar todas as carteiras", responses = {
        @ApiResponse(responseCode = "200", description = "Lista de carteiras retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<WalletDTO>> findAll() {
        List<Wallet> wallets = findAllWalletsUseCase.findAll();
        return ResponseEntity.ok(walletMapper.toDTOList(wallets));
    }

    @Operation(summary = "Obter saldo atual da carteira", responses = {
        @ApiResponse(responseCode = "200", description = "Saldo atual retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<WalletDTO> getBalance(@PathVariable UUID walletId) {
        Wallet wallet = createWalletUseCase.findById(walletId);
        return ResponseEntity.ok(walletMapper.toDTO(wallet));
    }

    @Operation(summary = "Obter saldo histórico da carteira", responses = {
        @ApiResponse(responseCode = "200", description = "Saldo histórico retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    @GetMapping("/{walletId}/balance/history")
    public ResponseEntity<WalletDTO> getHistoricalBalance(@PathVariable UUID walletId, @RequestParam("at") String at) {
        Wallet wallet = createWalletUseCase.findBalanceAt(walletId, at);
        return ResponseEntity.ok(walletMapper.toDTO(wallet));
    }

    @Operation(summary = "Transferir fundos entre carteiras", responses = {
        @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação ou saldo insuficiente"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequestDTO request) {
        transferFundsUseCase.transfer(request.getFromWalletId(), request.getToWalletId(), request.getAmount());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Criar nova carteira", responses = {
        @ApiResponse(responseCode = "201", description = "Carteira criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    @PostMapping
    public ResponseEntity<WalletDTO> create(@Valid @RequestBody CreateWalletRequestDTO dto) {
        Wallet wallet = walletMapper.toDomain(dto);
        Wallet created = createWalletUseCase.create(wallet);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletMapper.toDTO(created));
    }

    @Operation(summary = "Depositar em carteira", responses = {
        @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação ou carteira não encontrada")
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionDTO> deposit(@Valid @RequestBody DepositRequestDTO dto) {
        Transaction transaction = depositUseCase.deposit(dto.getWalletId(), dto.getAmount());
        return ResponseEntity.ok(transactionMapper.toDTO(transaction));
    }

    @Operation(summary = "Sacar da carteira", responses = {
        @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação, carteira não encontrada ou saldo insuficiente")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDTO> withdraw(@Valid @RequestBody WithdrawRequestDTO dto) {
        Transaction transaction = withdrawUseCase.withdraw(dto.getWalletId(), dto.getAmount());
        return ResponseEntity.ok(transactionMapper.toDTO(transaction));
    }
}
