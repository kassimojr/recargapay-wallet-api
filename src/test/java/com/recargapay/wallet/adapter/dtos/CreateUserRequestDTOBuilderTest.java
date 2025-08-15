package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateUserRequestDTO Builder Tests")
class CreateUserRequestDTOBuilderTest {

    @Test
    @DisplayName("Should build CreateUserRequestDTO with all fields using builder")
    void shouldBuildCreateUserRequestDTOWithAllFieldsUsingBuilder() {
        // Given
        String name = "John Doe";
        String email = "john.doe@example.com";

        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .name(name)
                .email(email)
                .build();

        // Then
        assertThat(requestDTO.getName()).isEqualTo(name);
        assertThat(requestDTO.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should build CreateUserRequestDTO with name only")
    void shouldBuildCreateUserRequestDTOWithNameOnly() {
        // Given
        String name = "Jane Doe";

        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .name(name)
                .build();

        // Then
        assertThat(requestDTO.getName()).isEqualTo(name);
        assertThat(requestDTO.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should build CreateUserRequestDTO with email only")
    void shouldBuildCreateUserRequestDTOWithEmailOnly() {
        // Given
        String email = "test@example.com";

        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .email(email)
                .build();

        // Then
        assertThat(requestDTO.getEmail()).isEqualTo(email);
        assertThat(requestDTO.getName()).isNull();
    }

    @Test
    @DisplayName("Should build CreateUserRequestDTO with null values")
    void shouldBuildCreateUserRequestDTOWithNullValues() {
        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .name(null)
                .email(null)
                .build();

        // Then
        assertThat(requestDTO.getName()).isNull();
        assertThat(requestDTO.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should build CreateUserRequestDTO with empty strings")
    void shouldBuildCreateUserRequestDTOWithEmptyStrings() {
        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .name("")
                .email("")
                .build();

        // Then
        assertThat(requestDTO.getName()).isEmpty();
        assertThat(requestDTO.getEmail()).isEmpty();
    }

    @Test
    @DisplayName("Should create multiple CreateUserRequestDTO instances with builder")
    void shouldCreateMultipleCreateUserRequestDTOInstancesWithBuilder() {
        // When
        CreateUserRequestDTO requestDTO1 = CreateUserRequestDTO.builder()
                .name("User One")
                .email("user1@example.com")
                .build();

        CreateUserRequestDTO requestDTO2 = CreateUserRequestDTO.builder()
                .name("User Two")
                .email("user2@example.com")
                .build();

        // Then
        assertThat(requestDTO1.getName()).isEqualTo("User One");
        assertThat(requestDTO1.getEmail()).isEqualTo("user1@example.com");

        assertThat(requestDTO2.getName()).isEqualTo("User Two");
        assertThat(requestDTO2.getEmail()).isEqualTo("user2@example.com");

        assertThat(requestDTO1).isNotEqualTo(requestDTO2);
    }

    @Test
    @DisplayName("Should handle builder method chaining")
    void shouldHandleBuilderMethodChaining() {
        // Given
        String name = "Chained User";
        String email = "chained@example.com";

        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .name(name)
                .email(email)
                .build();

        // Then
        assertThat(requestDTO.getName()).isEqualTo(name);
        assertThat(requestDTO.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should build CreateUserRequestDTO with special characters in name")
    void shouldBuildCreateUserRequestDTOWithSpecialCharactersInName() {
        // Given
        String name = "José da Silva-Santos";
        String email = "jose.silva@example.com";

        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .name(name)
                .email(email)
                .build();

        // Then
        assertThat(requestDTO.getName()).isEqualTo(name);
        assertThat(requestDTO.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should build CreateUserRequestDTO with long strings")
    void shouldBuildCreateUserRequestDTOWithLongStrings() {
        // Given
        String longName = "A".repeat(100);
        String longEmail = "a".repeat(50) + "@" + "b".repeat(50) + ".com";

        // When
        CreateUserRequestDTO requestDTO = CreateUserRequestDTO.builder()
                .name(longName)
                .email(longEmail)
                .build();

        // Then
        assertThat(requestDTO.getName()).isEqualTo(longName);
        assertThat(requestDTO.getEmail()).isEqualTo(longEmail);
    }
}
