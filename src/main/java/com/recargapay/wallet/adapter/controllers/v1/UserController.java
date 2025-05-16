package com.recargapay.wallet.adapter.controllers.v1;

import com.recargapay.wallet.adapter.converters.UserMapper;
import com.recargapay.wallet.adapter.dtos.CreateUserRequestDTO;
import com.recargapay.wallet.adapter.dtos.UserDTO;
import com.recargapay.wallet.core.domain.User;
import com.recargapay.wallet.core.ports.in.CreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;

    public UserController(CreateUserUseCase createUserUseCase, UserMapper userMapper) {
        this.createUserUseCase = createUserUseCase;
        this.userMapper = userMapper;
    }

    @Operation(summary = "Criar novo usuário", responses = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação"),
            @ApiResponse(responseCode = "409", description = "Email já está em uso")
    })
    @PostMapping
    public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserRequestDTO dto) {
        User user = userMapper.toDomain(dto);
        User created = createUserUseCase.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(created));
    }

    @Operation(summary = "Obter usuário por ID", responses = {
            @ApiResponse(responseCode = "200", description = "Usuário retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> findById(@PathVariable UUID userId) {
        return createUserUseCase.findById(userId)
                .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
