package com.montes.url_shortener.infrastructure.url;

import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShortUrlRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    private User testUser;
    private ShortUrl testShortUrl1;
    private ShortUrl testShortUrl2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .publicId("test-public-id")
                .email("test@example.com")
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .build();
        
        entityManager.persistAndFlush(testUser);

        LocalDateTime now = LocalDateTime.now();
        
        testShortUrl1 = ShortUrl.builder()
                .originalUrl("https://example1.com")
                .code("code1")
                .user(testUser)
                .usageCount(10)
                .createdAt(now.minusDays(5))
                .expiration(now.plusDays(30))
                .build();

        testShortUrl2 = ShortUrl.builder()
                .originalUrl("https://example2.com")
                .code("code2")
                .user(testUser)
                .usageCount(25)
                .createdAt(now.minusDays(3))
                .expiration(null) // No expiration
                .build();
    }

    @Test
    void shouldSaveAndFindShortUrlById() {
        // When
        ShortUrl savedUrl = shortUrlRepository.save(testShortUrl1);
        ShortUrl foundUrl = entityManager.find(ShortUrl.class, savedUrl.getId());

        // Then
        assertThat(foundUrl).isNotNull();
        assertThat(foundUrl.getOriginalUrl()).isEqualTo("https://example1.com");
        assertThat(foundUrl.getCode()).isEqualTo("code1");
        assertThat(foundUrl.getUser()).isEqualTo(testUser);
        assertThat(foundUrl.getUsageCount()).isEqualTo(10);
    }

    @Test
    void shouldFindShortUrlByCode() {
        // Given
        entityManager.persistAndFlush(testShortUrl1);

        // When
        Optional<ShortUrl> foundUrl = shortUrlRepository.findByCode("code1");

        // Then
        assertThat(foundUrl).isPresent();
        assertThat(foundUrl.get().getOriginalUrl()).isEqualTo("https://example1.com");
        assertThat(foundUrl.get().getCode()).isEqualTo("code1");
        assertThat(foundUrl.get().getUsageCount()).isEqualTo(10);
    }

    @Test
    void shouldReturnEmptyWhenCodeNotFound() {
        // When
        Optional<ShortUrl> foundUrl = shortUrlRepository.findByCode("nonexistent");

        // Then
        assertThat(foundUrl).isEmpty();
    }

    @Test
    void shouldFindAllUrlsByUser() {
        // Given
        entityManager.persistAndFlush(testShortUrl1);
        entityManager.persistAndFlush(testShortUrl2);

        // When
        List<ShortUrl> userUrls = shortUrlRepository.findByUser(testUser);

        // Then
        assertThat(userUrls).hasSize(2);
        assertThat(userUrls).extracting(ShortUrl::getCode).containsExactlyInAnyOrder("code1", "code2");
        assertThat(userUrls).extracting(ShortUrl::getOriginalUrl)
                .containsExactlyInAnyOrder("https://example1.com", "https://example2.com");
    }

    @Test
    void shouldFindUrlsByUserOrderedByUsageCountDesc() {
        // Given
        entityManager.persistAndFlush(testShortUrl1); // usage: 10
        entityManager.persistAndFlush(testShortUrl2); // usage: 25

        // When
        List<ShortUrl> userUrls = shortUrlRepository.findByUserOrderByUsageCountDesc(testUser);

        // Then
        assertThat(userUrls).hasSize(2);
        assertThat(userUrls.get(0).getUsageCount()).isEqualTo(25); // Higher usage first
        assertThat(userUrls.get(0).getCode()).isEqualTo("code2");
        assertThat(userUrls.get(1).getUsageCount()).isEqualTo(10);
        assertThat(userUrls.get(1).getCode()).isEqualTo("code1");
    }

    @Test
    void shouldReturnEmptyListForUserWithNoUrls() {
        // Given
        User userWithNoUrls = User.builder()
                .publicId("empty-user-id")
                .email("empty@example.com")
                .password("password")
                .firstName("Empty")
                .lastName("User")
                .build();
        entityManager.persistAndFlush(userWithNoUrls);

        // When
        List<ShortUrl> userUrls = shortUrlRepository.findByUser(userWithNoUrls);

        // Then
        assertThat(userUrls).isEmpty();
    }

    @Test
    void shouldEnforceUniqueCode() {
        // Given
        entityManager.persistAndFlush(testShortUrl1);
        
        ShortUrl duplicateCodeUrl = ShortUrl.builder()
                .originalUrl("https://different.com")
                .code("code1") // Same code
                .user(testUser)
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        // When & Then
        try {
            shortUrlRepository.save(duplicateCodeUrl);
            entityManager.flush();
            assertThat(false).as("Expected constraint violation exception").isTrue();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    void shouldIncrementUsageCount() {
        // Given
        entityManager.persistAndFlush(testShortUrl1);
        int initialCount = testShortUrl1.getUsageCount();

        // When
        testShortUrl1.incrementUsage();
        shortUrlRepository.save(testShortUrl1);
        entityManager.flush();
        entityManager.clear();

        // Then
        ShortUrl updatedUrl = shortUrlRepository.findByCode("code1").orElseThrow();
        assertThat(updatedUrl.getUsageCount()).isEqualTo(initialCount + 1);
    }

    @Test
    void shouldUpdateExpiration() {
        // Given
        entityManager.persistAndFlush(testShortUrl1);
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(60);

        // When
        testShortUrl1.updateExpiration(newExpiration);
        shortUrlRepository.save(testShortUrl1);
        entityManager.flush();
        entityManager.clear();

        // Then
        ShortUrl updatedUrl = shortUrlRepository.findByCode("code1").orElseThrow();
        assertThat(updatedUrl.getExpiration()).isEqualTo(newExpiration);
    }
}
