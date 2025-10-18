package com.montes.url_shortener.presentation.url;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.montes.url_shortener.application.url.UrlShortenerService;
import com.montes.url_shortener.application.user.UserService;
import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlShortenerController.class)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlShortenerService urlShortenerService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldShortenUrlSuccessfully() throws Exception {
        // Given
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
        
        ShortUrl shortUrl = ShortUrl.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .code("abc12345")
                .user(testUser)
                .build();
        
        when(userService.findByEmail("test@example.com")).thenReturn(testUser);
        when(urlShortenerService.createShortUrl(anyString(), any(User.class))).thenReturn(shortUrl);

        UrlShortenerController.ShortenRequest request = new UrlShortenerController.ShortenRequest();
        request.setOriginalUrl("https://example.com");

        // When & Then
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("abc12345"));
    }

    @Test
    void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Given
        UrlShortenerController.ShortenRequest request = new UrlShortenerController.ShortenRequest();
        request.setOriginalUrl("https://example.com");

        // When & Then
        mockMvc.perform(post("/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRedirectSuccessfully() throws Exception {
        // Given
        String code = "abc12345";
        ShortUrl shortUrl = ShortUrl.builder()
                .id(1L)
                .originalUrl("https://example.com")
                .code(code)
                .build();
        
        when(urlShortenerService.isExpired(code)).thenReturn(false);
        when(urlShortenerService.incrementUsage(code)).thenReturn(true);
        when(urlShortenerService.getByCode(code)).thenReturn(Optional.of(shortUrl));

        // When & Then
        mockMvc.perform(get("/url/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void shouldReturnNotFoundWhenCodeDoesNotExist() throws Exception {
        // Given
        String code = "nonexistent";
        
        when(urlShortenerService.isExpired(code)).thenReturn(false);
        when(urlShortenerService.incrementUsage(code)).thenReturn(true);
        when(urlShortenerService.getByCode(code)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/url/{code}", code))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnGoneWhenUrlIsExpired() throws Exception {
        // Given
        String code = "expired123";
        
        when(urlShortenerService.isExpired(code)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/url/{code}", code))
                .andExpect(status().isGone());
    }

    @Test
    void shouldReturnTooManyRequestsWhenIncrementFails() throws Exception {
        // Given
        String code = "abc12345";
        
        when(urlShortenerService.isExpired(code)).thenReturn(false);
        when(urlShortenerService.incrementUsage(code)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/url/{code}", code))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser
    void shouldUpdateExpirationSuccessfully() throws Exception {
        // Given
        String code = "abc12345";
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(7);
        
        when(urlShortenerService.updateExpiration(code, newExpiration)).thenReturn(true);

        UrlShortenerController.ExpirationRequest request = new UrlShortenerController.ExpirationRequest();
        request.setExpiration(newExpiration);

        // When & Then
        mockMvc.perform(patch("/url/{code}/expiration", code)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenUpdatingNonexistentUrl() throws Exception {
        // Given
        String code = "nonexistent";
        LocalDateTime newExpiration = LocalDateTime.now().plusDays(7);
        
        when(urlShortenerService.updateExpiration(code, newExpiration)).thenReturn(false);

        UrlShortenerController.ExpirationRequest request = new UrlShortenerController.ExpirationRequest();
        request.setExpiration(newExpiration);

        // When & Then
        mockMvc.perform(patch("/url/{code}/expiration", code)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
