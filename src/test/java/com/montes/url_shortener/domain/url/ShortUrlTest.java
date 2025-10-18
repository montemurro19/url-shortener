package com.montes.url_shortener.domain.url;

import com.montes.url_shortener.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUrlTest {

    private User testUser;
    private ShortUrl shortUrl;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .publicId("test-public-id")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        shortUrl = ShortUrl.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .code("abc12345")
                .user(testUser)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateShortUrlWithBuilder() {
        // Then
        assertThat(shortUrl.getId()).isEqualTo(1L);
        assertThat(shortUrl.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(shortUrl.getCode()).isEqualTo("abc12345");
        assertThat(shortUrl.getUser()).isEqualTo(testUser);
        assertThat(shortUrl.getUsageCount()).isEqualTo(0);
        assertThat(shortUrl.getCreatedAt()).isNotNull();
        assertThat(shortUrl.getExpiration()).isNull();
    }

    @Test
    void shouldIncrementUsageCount() {
        // Given
        int initialCount = shortUrl.getUsageCount();

        // When
        shortUrl.incrementUsage();

        // Then
        assertThat(shortUrl.getUsageCount()).isEqualTo(initialCount + 1);
    }

    @Test
    void shouldIncrementUsageMultipleTimes() {
        // Given
        int initialCount = shortUrl.getUsageCount();

        // When
        shortUrl.incrementUsage();
        shortUrl.incrementUsage();
        shortUrl.incrementUsage();

        // Then
        assertThat(shortUrl.getUsageCount()).isEqualTo(initialCount + 3);
    }

    @Test
    void shouldUpdateExpiration() {
        // Given
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(30);

        // When
        shortUrl.updateExpiration(newExpiration);

        // Then
        assertThat(shortUrl.getExpiration()).isEqualTo(newExpiration);
    }

    @Test
    void shouldUpdateExpirationToNull() {
        // Given
        shortUrl.setExpiration(LocalDateTime.now().plusDays(30));
        assertThat(shortUrl.getExpiration()).isNotNull();

        // When
        shortUrl.updateExpiration(null);

        // Then
        assertThat(shortUrl.getExpiration()).isNull();
    }

    @Test
    void shouldHaveDefaultUsageCountZero() {
        // Given
        ShortUrl newUrl = ShortUrl.builder()
                .originalUrl("https://test.com")
                .code("test123")
                .user(testUser)
                .build();

        // Then
        assertThat(newUrl.getUsageCount()).isEqualTo(0);
    }

    @Test
    void shouldHaveDefaultCreatedAtAsNow() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        ShortUrl newUrl = ShortUrl.builder()
                .originalUrl("https://test.com")
                .code("test123")
                .user(testUser)
                .build();
        
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(newUrl.getCreatedAt()).isNotNull();
        assertThat(newUrl.getCreatedAt()).isAfter(before);
        assertThat(newUrl.getCreatedAt()).isBefore(after);
    }

    @Test
    void shouldAllowCustomCreatedAt() {
        // Given
        LocalDateTime customTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        
        ShortUrl newUrl = ShortUrl.builder()
                .originalUrl("https://test.com")
                .code("test123")
                .user(testUser)
                .createdAt(customTime)
                .build();

        // Then
        assertThat(newUrl.getCreatedAt()).isEqualTo(customTime);
    }

    @Test
    void shouldSupportEqualsAndHashCode() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        
        ShortUrl url1 = ShortUrl.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .code("abc123")
                .user(testUser)
                .usageCount(0)
                .createdAt(fixedTime)
                .build();

        ShortUrl url2 = ShortUrl.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .code("abc123")
                .user(testUser)
                .usageCount(0)
                .createdAt(fixedTime)
                .build();

        // Then
        assertThat(url1).isEqualTo(url2);
        assertThat(url1.hashCode()).isEqualTo(url2.hashCode());
    }

    @Test
    void shouldSupportToString() {
        // When
        String toString = shortUrl.toString();

        // Then
        assertThat(toString).isNotNull();
        assertThat(toString).contains("ShortUrl");
        assertThat(toString).contains("abc12345");
        assertThat(toString).contains("https://example.com");
    }
}
