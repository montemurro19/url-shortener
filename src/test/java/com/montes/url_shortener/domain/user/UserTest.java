package com.montes.url_shortener.domain.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateUserWithBuilder() {
        // When
        User user = User.builder()
                .id(1L)
                .publicId("test-public-id")
                .email("test@example.com")
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .build();

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getPublicId()).isEqualTo("test-public-id");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
    }

    @Test
    void shouldGeneratePublicIdByDefault() {
        // When
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();

        // Then
        assertThat(user.getPublicId()).isNotNull();
        assertThat(user.getPublicId()).isNotEmpty();
        assertThat(user.getPublicId()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    void shouldAllowCustomPublicId() {
        // When
        User user = User.builder()
                .publicId("custom-public-id")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();

        // Then
        assertThat(user.getPublicId()).isEqualTo("custom-public-id");
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .publicId("same-id")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();

        User user2 = User.builder()
                .id(1L)
                .publicId("same-id")
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();

        // Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldSupportToString() {
        // Given
        User user = User.builder()
                .id(1L)
                .publicId("test-public-id")
                .email("test@example.com")
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .build();

        // When
        String toString = user.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("User");
        assertThat(toString).contains("test@example.com");
        assertThat(toString).contains("Test");
        assertThat(toString).contains("User");
    }

    @Test
    void shouldCreateUserWithNoArgsConstructor() {
        // When
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password");

        // Then
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getPublicId()).isNotNull(); // Should have default UUID
    }

    @Test
    void shouldCreateUserWithAllArgsConstructor() {
        // When
        User user = new User(1L, "custom-id", "test@example.com", "password", "Test", "User");

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getPublicId()).isEqualTo("custom-id");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
    }
}
