package com.recargapay.wallet.adapter.controllers.v1;

import com.recargapay.wallet.core.ports.in.TransferFundsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final TransferFundsUseCase transferFundsUseCase;

    public WalletController(TransferFundsUseCase transferFundsUseCase) {
        this.transferFundsUseCase = transferFundsUseCase;
    }

    @Operation(summary = "Transferir fundos entre carteiras", responses = {
        @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação ou saldo insuficiente"),
        @ApiResponse(responseCode = "404", description = "Carteira não encontrada")
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestParam UUID fromWalletId,
                                         @RequestParam UUID toWalletId,
                                         @RequestParam BigDecimal amount) {
        transferFundsUseCase.transfer(fromWalletId, toWalletId, amount);
        return ResponseEntity.ok().build();
    }
}
