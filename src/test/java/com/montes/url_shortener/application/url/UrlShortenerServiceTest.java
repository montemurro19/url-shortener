package com.montes.url_shortener.application.url;

import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import com.montes.url_shortener.infrastructure.url.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @InjectMocks
    private UrlShortenerService urlShortenerService;

    private User testUser;
    private ShortUrl testShortUrl;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .publicId("test-public-id")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        testShortUrl = ShortUrl.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .code("abc12345")
                .user(testUser)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateShortUrlSuccessfully() {
        // Given
        String originalUrl = "https://example.com";
        when(shortUrlRepository.save(any(ShortUrl.class))).thenReturn(testShortUrl);

        // When
        ShortUrl result = urlShortenerService.createShortUrl(originalUrl, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo(originalUrl);
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getCode()).isNotNull();
        verify(shortUrlRepository).save(any(ShortUrl.class));
    }

    @Test
    void shouldReturnShortUrlByCode() {
        // Given
        String code = "abc12345";
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.of(testShortUrl));

        // When
        Optional<ShortUrl> result = urlShortenerService.getByCode(code);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testShortUrl);
        verify(shortUrlRepository).findByCode(code);
    }

    @Test
    void shouldReturnEmptyWhenCodeNotFound() {
        // Given
        String code = "nonexistent";
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.empty());

        // When
        Optional<ShortUrl> result = urlShortenerService.getByCode(code);

        // Then
        assertThat(result).isEmpty();
        verify(shortUrlRepository).findByCode(code);
    }

    @Test
    void shouldIncrementUsageSuccessfully() {
        // Given
        String code = "abc12345";
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.of(testShortUrl));
        when(shortUrlRepository.save(testShortUrl)).thenReturn(testShortUrl);

        // When
        boolean result = urlShortenerService.incrementUsage(code);

        // Then
        assertThat(result).isTrue();
        assertThat(testShortUrl.getUsageCount()).isEqualTo(1);
        verify(shortUrlRepository).findByCode(code);
        verify(shortUrlRepository).save(testShortUrl);
    }

    @Test
    void shouldReturnFalseWhenIncrementingNonexistentUrl() {
        // Given
        String code = "nonexistent";
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.empty());

        // When
        boolean result = urlShortenerService.incrementUsage(code);

        // Then
        assertThat(result).isFalse();
        verify(shortUrlRepository).findByCode(code);
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldUpdateExpirationSuccessfully() {
        // Given
        String code = "abc12345";
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(7);
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.of(testShortUrl));
        when(shortUrlRepository.save(testShortUrl)).thenReturn(testShortUrl);

        // When
        boolean result = urlShortenerService.updateExpiration(code, newExpiration);

        // Then
        assertThat(result).isTrue();
        assertThat(testShortUrl.getExpiration()).isEqualTo(newExpiration);
        verify(shortUrlRepository).findByCode(code);
        verify(shortUrlRepository).save(testShortUrl);
    }

    @Test
    void shouldReturnFalseWhenUpdatingExpirationOfNonexistentUrl() {
        // Given
        String code = "nonexistent";
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(7);
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.empty());

        // When
        boolean result = urlShortenerService.updateExpiration(code, newExpiration);

        // Then
        assertThat(result).isFalse();
        verify(shortUrlRepository).findByCode(code);
        verify(shortUrlRepository, never()).save(any());
    }

    @Test
    void shouldReturnTrueWhenUrlIsExpired() {
        // Given
        String code = "abc12345";
        testShortUrl.setExpiration(LocalDateTime.now().minusDays(1)); // Expired
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.of(testShortUrl));

        // When
        boolean result = urlShortenerService.isExpired(code);

        // Then
        assertThat(result).isTrue();
        verify(shortUrlRepository).findByCode(code);
    }

    @Test
    void shouldReturnFalseWhenUrlIsNotExpired() {
        // Given
        String code = "abc12345";
        testShortUrl.setExpiration(LocalDateTime.now().plusDays(1)); // Not expired
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.of(testShortUrl));

        // When
        boolean result = urlShortenerService.isExpired(code);

        // Then
        assertThat(result).isFalse();
        verify(shortUrlRepository).findByCode(code);
    }

    @Test
    void shouldReturnFalseWhenUrlHasNoExpiration() {
        // Given
        String code = "abc12345";
        testShortUrl.setExpiration(null); // No expiration
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.of(testShortUrl));

        // When
        boolean result = urlShortenerService.isExpired(code);

        // Then
        assertThat(result).isFalse();
        verify(shortUrlRepository).findByCode(code);
    }

    @Test
    void shouldReturnFalseWhenCheckingExpirationOfNonexistentUrl() {
        // Given
        String code = "nonexistent";
        when(shortUrlRepository.findByCode(code)).thenReturn(Optional.empty());

        // When
        boolean result = urlShortenerService.isExpired(code);

        // Then
        assertThat(result).isFalse();
        verify(shortUrlRepository).findByCode(code);
    }
}
