package com.montes.url_shortener.infrastructure.user;

import com.montes.url_shortener.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .publicId("test-public-id")
                .email("test@example.com")
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void shouldSaveAndFindUserById() {
        // When
        User savedUser = userRepository.save(testUser);
        User foundUser = entityManager.find(User.class, savedUser.getId());

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getFirstName()).isEqualTo("Test");
        assertThat(foundUser.getLastName()).isEqualTo("User");
        assertThat(foundUser.getPublicId()).isEqualTo("test-public-id");
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldFindUserByPublicId() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByPublicId("test-public-id");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPublicId()).isEqualTo("test-public-id");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenPublicIdNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByPublicId("nonexistent-public-id");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldGenerateUniquePublicIdAutomatically() {
        // Given
        User userWithoutPublicId = User.builder()
                .email("auto@example.com")
                .password("password")
                .firstName("Auto")
                .lastName("User")
                .build();

        // When
        User savedUser = userRepository.save(userWithoutPublicId);

        // Then
        assertThat(savedUser.getPublicId()).isNotNull();
        assertThat(savedUser.getPublicId()).isNotEmpty();
    }

    @Test
    void shouldEnforceUniqueEmail() {
        // Given
        entityManager.persistAndFlush(testUser);
        
        User duplicateEmailUser = User.builder()
                .publicId("different-public-id")
                .email("test@example.com") // Same email
                .password("different-password")
                .firstName("Different")
                .lastName("User")
                .build();

        // When & Then
        // This should throw a constraint violation exception
        try {
            userRepository.save(duplicateEmailUser);
            entityManager.flush();
            assertThat(false).as("Expected constraint violation exception").isTrue();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
