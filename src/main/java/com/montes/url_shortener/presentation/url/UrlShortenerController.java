package com.montes.url_shortener.presentation.url;

import com.montes.url_shortener.application.url.UrlShortenerService;
import com.montes.url_shortener.application.user.UserService;
import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/url")
@RequiredArgsConstructor
public class UrlShortenerController {
    private final UrlShortenerService urlShortenerService;
    private final UserService userService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shorten(@RequestBody ShortenRequest request, Authentication authentication) {
        System.out.println("UrlShortenerController - Authentication: " + authentication);
        if (authentication == null) {
            System.out.println("UrlShortenerController - Authentication is null!");
            return ResponseEntity.status(401).build();
        }
        System.out.println("UrlShortenerController - Authentication name: " + authentication.getName());
        User user = userService.findByEmail(authentication.getName());
        ShortUrl shortUrl = urlShortenerService.createShortUrl(request.getOriginalUrl(), user);
        return ResponseEntity.ok(new ShortenResponse(shortUrl.getCode()));
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        if (urlShortenerService.isExpired(code)) {
            return ResponseEntity.status(410).build();
        }

        boolean incremented = urlShortenerService.incrementUsage(code);
        if (!incremented) {
            return ResponseEntity.status(429).build();
        }

        return urlShortenerService.getByCode(code)
                .map(shortUrl -> ResponseEntity.status(302).location(URI.create(shortUrl.getOriginalUrl()))
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{code}/expiration")
    public ResponseEntity<Void> updateExpiration(@PathVariable String code, @RequestBody ExpirationRequest request) {
        boolean updated = urlShortenerService.updateExpiration(code, request.getExpiration());
        if (updated) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Data
    public static class ShortenRequest {
        private String originalUrl;
    }

    @Data
    public static class ShortenResponse {
        private final String code;
    }

    @Data
    public static class UsageResponse {
        private final int usageCount;
    }

    @Data
    public static class ExpirationRequest {
        private java.time.LocalDateTime expiration;
    }
}
