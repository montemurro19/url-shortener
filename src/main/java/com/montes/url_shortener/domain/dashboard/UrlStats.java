package com.montes.url_shortener.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlStats {
    private Long id;
    private String code;
    private String originalUrl;
    private int usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiration;
    private boolean isExpired;
}
