package com.montes.url_shortener.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private long totalUrls;
    private long totalClicks;
    private long activeUrls;
    private long expiredUrls;
    private double averageClicksPerUrl;
    private String mostClickedUrl;
    private int mostClickedUrlClicks;
}
