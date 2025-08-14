package com.montes.url_shortener.domain.url;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "short_urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.montes.url_shortener.domain.user.User user;

    @Column(nullable = false)
    @Builder.Default
    private int usageCount = 0;

    @Column
    private java.time.LocalDateTime expiration;

    public void incrementUsage() {
        this.usageCount++;
    }

    public void updateExpiration(java.time.LocalDateTime newExpiration) {
        this.expiration = newExpiration;
    }
}
