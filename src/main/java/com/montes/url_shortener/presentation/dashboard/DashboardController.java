package com.montes.url_shortener.presentation.dashboard;

import com.montes.url_shortener.application.dashboard.DashboardService;
import com.montes.url_shortener.domain.dashboard.DashboardSummary;
import com.montes.url_shortener.domain.dashboard.UrlStats;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getSummary(Authentication authentication) {
        String userPublicId = (String) authentication.getDetails();
        DashboardSummary summary = dashboardService.getDashboardSummary(userPublicId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/urls")
    public ResponseEntity<List<UrlStats>> getUserUrls(Authentication authentication) {
        String userPublicId = (String) authentication.getDetails();
        List<UrlStats> urlStats = dashboardService.getUserUrlStats(userPublicId);
        return ResponseEntity.ok(urlStats);
    }

    @GetMapping("/urls/top")
    public ResponseEntity<List<UrlStats>> getTopUrls(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        String userPublicId = (String) authentication.getDetails();
        List<UrlStats> topUrls = dashboardService.getTopPerformingUrls(userPublicId, limit);
        return ResponseEntity.ok(topUrls);
    }
}
