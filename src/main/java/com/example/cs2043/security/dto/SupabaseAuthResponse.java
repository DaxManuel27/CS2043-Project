package com.example.cs2043.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SupabaseAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    @JsonProperty("token_type")
    private String tokenType;
    
    @JsonProperty("user")
    private SupabaseUser user;
    
    @Data
    public static class SupabaseUser {
        private String id;
        private String email;
        
        @JsonProperty("user_metadata")
        private Map<String, Object> userMetadata;
        
        @JsonProperty("created_at")
        private String createdAt;
    }
}
