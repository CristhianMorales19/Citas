package com.example.proyectocitas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private UserDTO user;
    
    public boolean isSuccess() {
        return this.success;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public String getToken() {
        return this.token;
    }
    
    public UserDTO getUser() {
        return this.user;
    }
    
    // Builder method
    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }
    
    // Builder class
    public static class AuthResponseBuilder {
        private AuthResponse response = new AuthResponse();
        
        public AuthResponseBuilder success(boolean success) {
            response.success = success;
            return this;
        }
        
        public AuthResponseBuilder message(String message) {
            response.message = message;
            return this;
        }
        
        public AuthResponseBuilder token(String token) {
            response.token = token;
            return this;
        }
        
        public AuthResponseBuilder user(UserDTO user) {
            response.user = user;
            return this;
        }
        
        public AuthResponse build() {
            return response;
        }
    }
}
