package com.example.cs2043.controllers;

import com.example.cs2043.security.SupabaseAuthService;
import com.example.cs2043.security.dto.AuthResponse;
import com.example.cs2043.security.dto.LoginRequest;
import com.example.cs2043.security.dto.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private SupabaseAuthService supabaseAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = supabaseAuthService.login(
                loginRequest.getEmail(), 
                loginRequest.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> signupRequest) {
        try {
            String email = signupRequest.get("email");
            String password = signupRequest.get("password");
            String firstName = signupRequest.getOrDefault("firstName", "User");
            String lastName = signupRequest.getOrDefault("lastName", "");
            
            AuthResponse response = supabaseAuthService.signup(email, password, firstName, lastName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400)
                .body(Map.of("error", "Signup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null) {
            supabaseAuthService.logout(token);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            UserInfo user = supabaseAuthService.getUserFromToken(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Invalid token: " + e.getMessage()));
        }
    }
}
