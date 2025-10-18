package com.montes.url_shortener.application.url;

import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import com.montes.url_shortener.infrastructure.url.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {
    private final ShortUrlRepository shortUrlRepository;

    public ShortUrl createShortUrl(String originalUrl, User user) {
        String code = UUID.randomUUID().toString().substring(0, 8);
        ShortUrl shortUrl = ShortUrl.builder()
                .originalUrl(originalUrl)
                .code(code)
                .user(user)
                .build();
        return shortUrlRepository.save(shortUrl);
    }

    public Optional<ShortUrl> getByCode(String code) {
        return shortUrlRepository.findByCode(code);
    }

    public boolean incrementUsage(String code) {
        Optional<ShortUrl> optionalShortUrl = shortUrlRepository.findByCode(code);
        if (optionalShortUrl.isPresent()) {
            ShortUrl shortUrl = optionalShortUrl.get();
            shortUrl.incrementUsage();
            shortUrlRepository.save(shortUrl);
            return true;
        }
        return false;
    }

    public boolean updateExpiration(String code, java.time.LocalDateTime newExpiration) {
        Optional<ShortUrl> optionalShortUrl = shortUrlRepository.findByCode(code);
        if (optionalShortUrl.isPresent()) {
            ShortUrl shortUrl = optionalShortUrl.get();
            shortUrl.updateExpiration(newExpiration);
            shortUrlRepository.save(shortUrl);
            return true;
        }
        return false;
    }

    public boolean isExpired(String code) {
        Optional<ShortUrl> optionalShortUrl = shortUrlRepository.findByCode(code);
        if (optionalShortUrl.isPresent()) {
            ShortUrl shortUrl = optionalShortUrl.get();
            return shortUrl.getExpiration() != null && shortUrl.getExpiration().isBefore(java.time.LocalDateTime.now());
        }
        return false;
    }
}
