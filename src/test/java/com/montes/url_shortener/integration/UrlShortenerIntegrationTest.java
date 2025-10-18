package com.montes.url_shortener.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.montes.url_shortener.presentation.user.AuthController;
import com.montes.url_shortener.presentation.url.UrlShortenerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteUserJourneySuccessfully() throws Exception {
        // Step 1: Register a new user
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setEmail("integration@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Integration");
        registerRequest.setLastName("Test");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extract token from response
        String registerResponse = registerResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(registerResponse).get("token").asText();

        // Step 2: Login with the same user
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail("integration@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Step 3: Create a short URL
        UrlShortenerController.ShortenRequest shortenRequest = new UrlShortenerController.ShortenRequest();
        shortenRequest.setOriginalUrl("https://www.example.com/very-long-url");

        MvcResult shortenResult = mockMvc.perform(post("/api/url/shorten")
                .with(csrf())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andReturn();

        // Extract code from response
        String shortenResponse = shortenResult.getResponse().getContentAsString();
        String code = objectMapper.readTree(shortenResponse).get("code").asText();

        // Step 4: Use the short URL (redirect)
        mockMvc.perform(get("/api/url/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.example.com/very-long-url"));

        // Step 5: Use the short URL again to test usage increment
        mockMvc.perform(get("/api/url/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.example.com/very-long-url"));

        // Step 6: Get dashboard summary
        mockMvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUrls").value(1))
                .andExpect(jsonPath("$.totalClicks").value(2))
                .andExpect(jsonPath("$.activeUrls").value(1))
                .andExpect(jsonPath("$.expiredUrls").value(0));

        // Step 7: Get user URLs
        mockMvc.perform(get("/api/dashboard/urls")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value(code))
                .andExpect(jsonPath("$[0].originalUrl").value("https://www.example.com/very-long-url"))
                .andExpect(jsonPath("$[0].usageCount").value(2));
    }

    @Test
    void shouldReturnNotFoundForNonexistentShortUrl() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/url/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        // Create short URL without authentication
        UrlShortenerController.ShortenRequest shortenRequest = new UrlShortenerController.ShortenRequest();
        shortenRequest.setOriginalUrl("https://example.com");

        mockMvc.perform(post("/api/url/shorten")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortenRequest)))
                .andExpect(status().isUnauthorized());

        // Dashboard endpoints without authentication
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dashboard/urls"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleDuplicateEmailRegistration() throws Exception {
        // Step 1: Register first user
        AuthController.RegisterRequest registerRequest1 = new AuthController.RegisterRequest();
        registerRequest1.setEmail("duplicate@example.com");
        registerRequest1.setPassword("password123");
        registerRequest1.setFirstName("First");
        registerRequest1.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest1)))
                .andExpect(status().isOk());

        // Step 2: Try to register second user with same email
        AuthController.RegisterRequest registerRequest2 = new AuthController.RegisterRequest();
        registerRequest2.setEmail("duplicate@example.com");
        registerRequest2.setPassword("differentpassword");
        registerRequest2.setFirstName("Second");
        registerRequest2.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest2)))
                .andExpect(status().is5xxServerError()); // Should fail due to duplicate email
    }

    @Test
    void shouldHandleInvalidLogin() throws Exception {
        // Try to login with non-existent user
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError()); // Should fail due to invalid credentials
    }
}
