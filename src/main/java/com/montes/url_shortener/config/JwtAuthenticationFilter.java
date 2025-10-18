package com.montes.url_shortener.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Key key;
    
    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        System.out.println("JWT Filter - Request URI: " + request.getRequestURI());
        System.out.println("JWT Filter - Auth Header: " + (authHeader != null ? "Present" : "Not present"));
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                String email = claims.getSubject();
                String publicId = claims.get("publicId", String.class);
                System.out.println("JWT Filter - Email: " + email + ", PublicId: " + publicId);
                
                if (email != null) {
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                    auth.setDetails(publicId); // Store publicId in details for dashboard access
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("JWT Filter - Authentication set successfully");
                }
            } catch (Exception e) {
                System.out.println("JWT Filter - Token parsing failed: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
