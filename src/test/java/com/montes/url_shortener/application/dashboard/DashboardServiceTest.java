package com.montes.url_shortener.application.dashboard;

import com.montes.url_shortener.domain.dashboard.DashboardSummary;
import com.montes.url_shortener.domain.dashboard.UrlStats;
import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import com.montes.url_shortener.infrastructure.url.ShortUrlRepository;
import com.montes.url_shortener.infrastructure.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;
    private List<ShortUrl> testUrls;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .publicId("test-public-id")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        LocalDateTime now = LocalDateTime.now();
        
        testUrls = Arrays.asList(
            ShortUrl.builder()
                .id(1L)
                .originalUrl("https://example1.com")
                .code("code1")
                .user(testUser)
                .usageCount(10)
                .createdAt(now.minusDays(5))
                .expiration(now.plusDays(30))
                .build(),
            ShortUrl.builder()
                .id(2L)
                .originalUrl("https://example2.com")
                .code("code2")
                .user(testUser)
                .usageCount(25)
                .createdAt(now.minusDays(3))
                .expiration(now.minusDays(1)) // Expired
                .build(),
            ShortUrl.builder()
                .id(3L)
                .originalUrl("https://example3.com")
                .code("code3")
                .user(testUser)
                .usageCount(5)
                .createdAt(now.minusDays(1))
                .expiration(null) // No expiration
                .build()
        );
    }

    @Test
    void shouldGetDashboardSummarySuccessfully() {
        // Given
        String userPublicId = "test-public-id";
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.of(testUser));
        when(shortUrlRepository.findByUser(testUser)).thenReturn(testUrls);

        // When
        DashboardSummary result = dashboardService.getDashboardSummary(userPublicId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalUrls()).isEqualTo(3);
        assertThat(result.getTotalClicks()).isEqualTo(40); // 10 + 25 + 5
        assertThat(result.getActiveUrls()).isEqualTo(2); // URLs 1 and 3 are active
        assertThat(result.getExpiredUrls()).isEqualTo(1); // URL 2 is expired
        assertThat(result.getAverageClicksPerUrl()).isEqualTo(40.0 / 3.0);
        assertThat(result.getMostClickedUrl()).isEqualTo("https://example2.com");
        assertThat(result.getMostClickedUrlClicks()).isEqualTo(25);
        
        verify(userRepository).findByPublicId(userPublicId);
        verify(shortUrlRepository).findByUser(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForDashboardSummary() {
        // Given
        String userPublicId = "nonexistent-id";
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dashboardService.getDashboardSummary(userPublicId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
        
        verify(userRepository).findByPublicId(userPublicId);
    }

    @Test
    void shouldGetUserUrlStatsSuccessfully() {
        // Given
        String userPublicId = "test-public-id";
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.of(testUser));
        when(shortUrlRepository.findByUser(testUser)).thenReturn(testUrls);

        // When
        List<UrlStats> result = dashboardService.getUserUrlStats(userPublicId);

        // Then
        assertThat(result).hasSize(3);
        
        UrlStats stats1 = result.get(0);
        assertThat(stats1.getId()).isEqualTo(1L);
        assertThat(stats1.getCode()).isEqualTo("code1");
        assertThat(stats1.getOriginalUrl()).isEqualTo("https://example1.com");
        assertThat(stats1.getUsageCount()).isEqualTo(10);
        assertThat(stats1.isExpired()).isFalse();

        UrlStats stats2 = result.get(1);
        assertThat(stats2.getId()).isEqualTo(2L);
        assertThat(stats2.getCode()).isEqualTo("code2");
        assertThat(stats2.isExpired()).isTrue();

        UrlStats stats3 = result.get(2);
        assertThat(stats3.getId()).isEqualTo(3L);
        assertThat(stats3.getCode()).isEqualTo("code3");
        assertThat(stats3.isExpired()).isFalse();
        
        verify(userRepository).findByPublicId(userPublicId);
        verify(shortUrlRepository).findByUser(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForUrlStats() {
        // Given
        String userPublicId = "nonexistent-id";
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dashboardService.getUserUrlStats(userPublicId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
        
        verify(userRepository).findByPublicId(userPublicId);
    }

    @Test
    void shouldGetTopPerformingUrlsSuccessfully() {
        // Given
        String userPublicId = "test-public-id";
        int limit = 2;
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.of(testUser));
        when(shortUrlRepository.findByUser(testUser)).thenReturn(testUrls);

        // When
        List<UrlStats> result = dashboardService.getTopPerformingUrls(userPublicId, limit);

        // Then
        assertThat(result).hasSize(2);
        
        // Should be sorted by usage count descending
        assertThat(result.get(0).getUsageCount()).isEqualTo(25); // URL 2
        assertThat(result.get(1).getUsageCount()).isEqualTo(10); // URL 1
        
        verify(userRepository).findByPublicId(userPublicId);
        verify(shortUrlRepository).findByUser(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForTopUrls() {
        // Given
        String userPublicId = "nonexistent-id";
        int limit = 5;
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dashboardService.getTopPerformingUrls(userPublicId, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
        
        verify(userRepository).findByPublicId(userPublicId);
    }

    @Test
    void shouldHandleEmptyUrlListInDashboardSummary() {
        // Given
        String userPublicId = "test-public-id";
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.of(testUser));
        when(shortUrlRepository.findByUser(testUser)).thenReturn(Arrays.asList());

        // When
        DashboardSummary result = dashboardService.getDashboardSummary(userPublicId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalUrls()).isEqualTo(0);
        assertThat(result.getTotalClicks()).isEqualTo(0);
        assertThat(result.getActiveUrls()).isEqualTo(0);
        assertThat(result.getExpiredUrls()).isEqualTo(0);
        assertThat(result.getAverageClicksPerUrl()).isEqualTo(0.0);
        assertThat(result.getMostClickedUrl()).isEqualTo("N/A");
        assertThat(result.getMostClickedUrlClicks()).isEqualTo(0);
        
        verify(userRepository).findByPublicId(userPublicId);
        verify(shortUrlRepository).findByUser(testUser);
    }
}
