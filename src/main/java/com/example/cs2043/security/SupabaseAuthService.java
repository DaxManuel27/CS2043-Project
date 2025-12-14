package com.example.cs2043.security;

import com.example.cs2043.security.dto.AuthResponse;
import com.example.cs2043.security.dto.SupabaseAuthResponse;
import com.example.cs2043.security.dto.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sign up a new user in Supabase Auth
     */
    public AuthResponse signup(String email, String password, String firstName, String lastName) {
        String url = supabaseUrl + "/auth/v1/signup";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        
        // Add user metadata
        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("role", "Employee");
        body.put("data", userData);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<SupabaseAuthResponse> response = restTemplate.postForEntity(
                url, request, SupabaseAuthResponse.class
            );
            
            if (response.getBody() != null) {
                SupabaseAuthResponse supabaseResponse = response.getBody();
                

                if (supabaseResponse.getUser() == null) {

                    UserInfo userInfo = UserInfo.builder()
                        .id(null)
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .role("Employee")
                        .build();
                    
                    return AuthResponse.builder()
                        .token(null)
                        .refreshToken(null)
                        .user(userInfo)
                        .expiresIn(null)
                        .message("Please check your email to confirm your account")
                        .build();
                }
                
                return mapToAuthResponse(supabaseResponse);
            } else {
                throw new RuntimeException("No response from Supabase");
            }
        } catch (Exception e) {
            throw new RuntimeException("Signup failed: " + e.getMessage(), e);
        }
    }

    public AuthResponse login(String email, String password) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<SupabaseAuthResponse> response = restTemplate.postForEntity(
                url, request, SupabaseAuthResponse.class
            );
            
            if (response.getBody() != null) {
                return mapToAuthResponse(response.getBody());
            } else {
                throw new RuntimeException("No response from Supabase");
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            // Check for specific Supabase error codes and provide user-friendly messages
            if (errorMessage != null && errorMessage.contains("email_not_confirmed")) {
                throw new RuntimeException("Email not confirmed. Please check your inbox and confirm your email before logging in.");
            } else if (errorMessage != null && errorMessage.contains("invalid_credentials")) {
                throw new RuntimeException("Invalid email or password.");
            }
            throw new RuntimeException("Login failed: " + errorMessage, e);
        }
    }

    public void logout(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        String url = supabaseUrl + "/auth/v1/logout";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
        } catch (Exception e) {
            // Log but don't throw - logout should be idempotent
            System.err.println("Logout warning: " + e.getMessage());
        }
    }

    public UserInfo getUserFromToken(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            return extractUserFromClaims(claims);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token: " + e.getMessage(), e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private AuthResponse mapToAuthResponse(SupabaseAuthResponse supabaseResponse) {
        UserInfo userInfo = UserInfo.builder()
            .id(supabaseResponse.getUser().getId())
            .email(supabaseResponse.getUser().getEmail())
            .firstName(extractMetadata(supabaseResponse.getUser(), "firstName", "User"))
            .lastName(extractMetadata(supabaseResponse.getUser(), "lastName", ""))
            .role(extractMetadata(supabaseResponse.getUser(), "role", "Employee"))
            .build();
        
        return AuthResponse.builder()
            .token(supabaseResponse.getAccessToken())
            .refreshToken(supabaseResponse.getRefreshToken())
            .user(userInfo)
            .expiresIn(supabaseResponse.getExpiresIn())
            .build();
    }

    private UserInfo extractUserFromClaims(Claims claims) {
        String userId = claims.getSubject();
        String email = claims.get("email", String.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userMetadata = claims.get("user_metadata", Map.class);
        
        String firstName = "User";
        String lastName = "";
        String role = "Employee";
        
        if (userMetadata != null) {
            firstName = (String) userMetadata.getOrDefault("firstName", "User");
            lastName = (String) userMetadata.getOrDefault("lastName", "");
            role = (String) userMetadata.getOrDefault("role", "Employee");
        }
        
        return UserInfo.builder()
            .id(userId)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .role(role)
            .build();
    }

    private String extractMetadata(SupabaseAuthResponse.SupabaseUser user, String key, String defaultValue) {
        if (user.getUserMetadata() != null && user.getUserMetadata().containsKey(key)) {
            Object value = user.getUserMetadata().get(key);
            return value != null ? value.toString() : defaultValue;
        }
        return defaultValue;
    }
}
