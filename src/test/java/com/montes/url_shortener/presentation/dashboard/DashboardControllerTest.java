package com.montes.url_shortener.presentation.dashboard;

import com.montes.url_shortener.application.dashboard.DashboardService;
import com.montes.url_shortener.domain.dashboard.DashboardSummary;
import com.montes.url_shortener.domain.dashboard.UrlStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser
    void shouldGetDashboardSummarySuccessfully() throws Exception {
        // Given
        DashboardSummary summary = new DashboardSummary(
                10L, // totalUrls
                150L, // totalClicks
                8L, // activeUrls
                2L, // expiredUrls
                15.0, // averageClicksPerUrl
                "https://example.com", // mostClickedUrl
                25 // mostClickedUrlClicks
        );
        
        when(dashboardService.getDashboardSummary(anyString())).thenReturn(summary);

        // When & Then
        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUrls").value(10))
                .andExpect(jsonPath("$.totalClicks").value(150))
                .andExpect(jsonPath("$.activeUrls").value(8))
                .andExpect(jsonPath("$.expiredUrls").value(2))
                .andExpect(jsonPath("$.averageClicksPerUrl").value(15.0))
                .andExpect(jsonPath("$.mostClickedUrl").value("https://example.com"))
                .andExpect(jsonPath("$.mostClickedUrlClicks").value(25));
    }

    @Test
    @WithMockUser
    void shouldGetUserUrlsSuccessfully() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<UrlStats> urlStats = Arrays.asList(
                new UrlStats(1L, "code1", "https://example1.com", 10, now.minusDays(5), null, false),
                new UrlStats(2L, "code2", "https://example2.com", 25, now.minusDays(3), now.minusDays(1), true)
        );
        
        when(dashboardService.getUserUrlStats(anyString())).thenReturn(urlStats);

        // When & Then
        mockMvc.perform(get("/dashboard/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("code1"))
                .andExpect(jsonPath("$[0].originalUrl").value("https://example1.com"))
                .andExpect(jsonPath("$[0].usageCount").value(10))
                .andExpect(jsonPath("$[0].expired").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].code").value("code2"))
                .andExpect(jsonPath("$[1].expired").value(true));
    }

    @Test
    @WithMockUser
    void shouldGetTopUrlsSuccessfully() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<UrlStats> topUrls = Arrays.asList(
                new UrlStats(2L, "code2", "https://example2.com", 25, now.minusDays(3), null, false),
                new UrlStats(1L, "code1", "https://example1.com", 10, now.minusDays(5), null, false)
        );
        
        when(dashboardService.getTopPerformingUrls(anyString(), eq(5))).thenReturn(topUrls);

        // When & Then
        mockMvc.perform(get("/dashboard/urls/top")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].usageCount").value(25)) // First should have higher usage
                .andExpect(jsonPath("$[1].usageCount").value(10));
    }

    @Test
    @WithMockUser
    void shouldGetTopUrlsWithDefaultLimit() throws Exception {
        // Given
        List<UrlStats> topUrls = Arrays.asList();
        
        when(dashboardService.getTopPerformingUrls(anyString(), eq(10))).thenReturn(topUrls);

        // When & Then
        mockMvc.perform(get("/dashboard/urls/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/dashboard/urls"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/dashboard/urls/top"))
                .andExpect(status().isUnauthorized());
    }
}
