package com.digital.wallet.adapter.controllers.v1;

import com.digital.wallet.adapter.converters.UserMapper;
import com.digital.wallet.adapter.dtos.CreateUserRequestDTO;
import com.digital.wallet.adapter.dtos.UserDTO;
import com.digital.wallet.core.domain.User;
import com.digital.wallet.core.ports.in.CreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsible for user operations.
 * Manages the creation and querying of users in the system.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "API for user management")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;

    public UserController(CreateUserUseCase createUserUseCase, UserMapper userMapper) {
        this.createUserUseCase = createUserUseCase;
        this.userMapper = userMapper;
    }

    /**
     * Creates a new user in the system.
     * 
     * @param dto Information of the user to be created
     * @return Data of the created user
     */
    @Operation(
        summary = "Create new user",
        description = "Creates a new user with the provided data and returns the created user data including its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "User created successfully",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid data provided",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(example = "{\"error\": \"Invalid data\", \"details\": [\"Email is required\", \"Name is required\"]}"))
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email already in use",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(example = "{\"error\": \"Email already in use\"}"))
        )
    })
    @PostMapping
    public ResponseEntity<UserDTO> create(
            @Valid 
            @org.springframework.web.bind.annotation.RequestBody 
            @RequestBody(
                description = "Data of the user to be created",
                required = true,
                content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = CreateUserRequestDTO.class)))
            CreateUserRequestDTO dto) {
        User user = userMapper.toDomain(dto);
        User created = createUserUseCase.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(created));
    }

    /**
     * Finds a user by its ID.
     * 
     * @param userId ID of the user to be found
     * @return Data of the found user or 404 if it doesn't exist
     */
    @Operation(
        summary = "Get user by ID",
        description = "Finds a specific user by its unique identifier (UUID)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User found successfully",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserDTO.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "User not found",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(example = "{}"))
        )
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> findById(
            @PathVariable(name = "userId", required = true) 
            @io.swagger.v3.oas.annotations.Parameter(
                description = "Unique identifier of the user (UUID)",
                required = true,
                schema = @Schema(type = "string", format = "uuid", example = "123e4567-e89b-12d3-a456-426614174000"))
            UUID userId) {
        return createUserUseCase.findById(userId)
                .map(user -> ResponseEntity.ok(userMapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
