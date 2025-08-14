package com.montes.url_shortener.presentation.user;

import com.montes.url_shortener.application.user.UserService;
import com.montes.url_shortener.domain.user.User;
import com.montes.url_shortener.infrastructure.user.TokenUtil;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final TokenUtil tokenUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request.getEmail(), request.getPassword(), request.getFirstName(),
                request.getLastName());
        String token = tokenUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = tokenUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class AuthResponse {
        private final String token;
    }
}
