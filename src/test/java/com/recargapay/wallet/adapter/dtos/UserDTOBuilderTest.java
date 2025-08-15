package com.recargapay.wallet.adapter.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserDTO Builder Tests")
class UserDTOBuilderTest {

    @Test
    @DisplayName("Should build UserDTO with all fields using builder")
    void shouldBuildUserDTOWithAllFieldsUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "John Doe";
        String email = "john.doe@example.com";

        // When
        UserDTO userDTO = UserDTO.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();

        // Then
        assertThat(userDTO.getId()).isEqualTo(id);
        assertThat(userDTO.getName()).isEqualTo(name);
        assertThat(userDTO.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should build UserDTO with minimal fields")
    void shouldBuildUserDTOWithMinimalFields() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "Jane Doe";

        // When
        UserDTO userDTO = UserDTO.builder()
                .id(id)
                .name(name)
                .build();

        // Then
        assertThat(userDTO.getId()).isEqualTo(id);
        assertThat(userDTO.getName()).isEqualTo(name);
        assertThat(userDTO.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should build UserDTO with null values")
    void shouldBuildUserDTOWithNullValues() {
        // When
        UserDTO userDTO = UserDTO.builder()
                .id(null)
                .name(null)
                .email(null)
                .build();

        // Then
        assertThat(userDTO.getId()).isNull();
        assertThat(userDTO.getName()).isNull();
        assertThat(userDTO.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should build UserDTO with email only")
    void shouldBuildUserDTOWithEmailOnly() {
        // Given
        String email = "test@example.com";

        // When
        UserDTO userDTO = UserDTO.builder()
                .email(email)
                .build();

        // Then
        assertThat(userDTO.getEmail()).isEqualTo(email);
        assertThat(userDTO.getId()).isNull();
        assertThat(userDTO.getName()).isNull();
    }

    @Test
    @DisplayName("Should build UserDTO with name only")
    void shouldBuildUserDTOWithNameOnly() {
        // Given
        String name = "Test User";

        // When
        UserDTO userDTO = UserDTO.builder()
                .name(name)
                .build();

        // Then
        assertThat(userDTO.getName()).isEqualTo(name);
        assertThat(userDTO.getId()).isNull();
        assertThat(userDTO.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should create multiple UserDTO instances with builder")
    void shouldCreateMultipleUserDTOInstancesWithBuilder() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        // When
        UserDTO userDTO1 = UserDTO.builder()
                .id(id1)
                .name("User One")
                .email("user1@example.com")
                .build();

        UserDTO userDTO2 = UserDTO.builder()
                .id(id2)
                .name("User Two")
                .email("user2@example.com")
                .build();

        // Then
        assertThat(userDTO1.getId()).isEqualTo(id1);
        assertThat(userDTO1.getName()).isEqualTo("User One");
        assertThat(userDTO1.getEmail()).isEqualTo("user1@example.com");

        assertThat(userDTO2.getId()).isEqualTo(id2);
        assertThat(userDTO2.getName()).isEqualTo("User Two");
        assertThat(userDTO2.getEmail()).isEqualTo("user2@example.com");

        assertThat(userDTO1).isNotEqualTo(userDTO2);
    }

    @Test
    @DisplayName("Should handle builder method chaining")
    void shouldHandleBuilderMethodChaining() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "Chained User";
        String email = "chained@example.com";

        // When
        UserDTO userDTO = UserDTO.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();

        // Then
        assertThat(userDTO.getId()).isEqualTo(id);
        assertThat(userDTO.getName()).isEqualTo(name);
        assertThat(userDTO.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should build UserDTO with special characters in name")
    void shouldBuildUserDTOWithSpecialCharactersInName() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "José da Silva-Santos";
        String email = "jose.silva@example.com";

        // When
        UserDTO userDTO = UserDTO.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();

        // Then
        assertThat(userDTO.getId()).isEqualTo(id);
        assertThat(userDTO.getName()).isEqualTo(name);
        assertThat(userDTO.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should build UserDTO with long strings")
    void shouldBuildUserDTOWithLongStrings() {
        // Given
        UUID id = UUID.randomUUID();
        String longName = "A".repeat(100);
        String longEmail = "a".repeat(50) + "@" + "b".repeat(50) + ".com";

        // When
        UserDTO userDTO = UserDTO.builder()
                .id(id)
                .name(longName)
                .email(longEmail)
                .build();

        // Then
        assertThat(userDTO.getId()).isEqualTo(id);
        assertThat(userDTO.getName()).isEqualTo(longName);
        assertThat(userDTO.getEmail()).isEqualTo(longEmail);
    }
}
