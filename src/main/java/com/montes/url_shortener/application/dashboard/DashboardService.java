package com.montes.url_shortener.application.dashboard;

import com.montes.url_shortener.domain.dashboard.DashboardSummary;
import com.montes.url_shortener.domain.dashboard.UrlStats;
import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import com.montes.url_shortener.infrastructure.url.ShortUrlRepository;
import com.montes.url_shortener.infrastructure.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;

    public DashboardSummary getDashboardSummary(String userPublicId) {
        Optional<User> userOpt = userRepository.findByPublicId(userPublicId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        List<ShortUrl> userUrls = shortUrlRepository.findByUser(user);

        long totalUrls = userUrls.size();
        long totalClicks = userUrls.stream().mapToLong(ShortUrl::getUsageCount).sum();
        long activeUrls = userUrls.stream()
                .filter(url -> url.getExpiration() == null || url.getExpiration().isAfter(LocalDateTime.now()))
                .count();
        long expiredUrls = totalUrls - activeUrls;

        double averageClicksPerUrl = totalUrls > 0 ? (double) totalClicks / totalUrls : 0;

        Optional<ShortUrl> mostClicked = userUrls.stream()
                .max((url1, url2) -> Integer.compare(url1.getUsageCount(), url2.getUsageCount()));

        String mostClickedUrl = mostClicked.map(ShortUrl::getOriginalUrl).orElse("N/A");
        int mostClickedUrlClicks = mostClicked.map(ShortUrl::getUsageCount).orElse(0);

        return new DashboardSummary(
                totalUrls,
                totalClicks,
                activeUrls,
                expiredUrls,
                averageClicksPerUrl,
                mostClickedUrl,
                mostClickedUrlClicks
        );
    }

    public List<UrlStats> getUserUrlStats(String userPublicId) {
        Optional<User> userOpt = userRepository.findByPublicId(userPublicId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        List<ShortUrl> userUrls = shortUrlRepository.findByUser(user);

        return userUrls.stream()
                .map(url -> new UrlStats(
                        url.getId(),
                        url.getCode(),
                        url.getOriginalUrl(),
                        url.getUsageCount(),
                        url.getCreatedAt(),
                        url.getExpiration(),
                        url.getExpiration() != null && url.getExpiration().isBefore(LocalDateTime.now())
                ))
                .collect(Collectors.toList());
    }

    public List<UrlStats> getTopPerformingUrls(String userPublicId, int limit) {
        Optional<User> userOpt = userRepository.findByPublicId(userPublicId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        List<ShortUrl> userUrls = shortUrlRepository.findByUser(user);

        return userUrls.stream()
                .sorted((url1, url2) -> Integer.compare(url2.getUsageCount(), url1.getUsageCount()))
                .limit(limit)
                .map(url -> new UrlStats(
                        url.getId(),
                        url.getCode(),
                        url.getOriginalUrl(),
                        url.getUsageCount(),
                        url.getCreatedAt(),
                        url.getExpiration(),
                        url.getExpiration() != null && url.getExpiration().isBefore(LocalDateTime.now())
                ))
                .collect(Collectors.toList());
    }
}
