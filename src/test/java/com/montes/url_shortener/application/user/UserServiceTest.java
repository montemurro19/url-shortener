package com.montes.url_shortener.application.user;

import com.montes.url_shortener.domain.user.User;
import com.montes.url_shortener.infrastructure.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .publicId("test-public-id")
                .email("test@example.com")
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        // Given
        String email = "new@example.com";
        String password = "password123";
        String firstName = "New";
        String lastName = "User";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.register(email, password, firstName, lastName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.register(email, "password", "First", "Last"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");
        
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // When
        User result = userService.login(email, password);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.login(email, "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");
        
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Given
        String email = "test@example.com";
        String password = "wrongpassword";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.login(email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");
        
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.findByEmail(email);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findByEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
        
        verify(userRepository).findByEmail(email);
    }
}
