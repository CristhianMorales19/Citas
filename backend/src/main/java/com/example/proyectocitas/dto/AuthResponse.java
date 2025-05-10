package com.example.proyectocitas.dto;

import com.example.proyectocitas.models.AppUser;
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private AppUser user;
    private String username;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String token, AppUser user) {
        this.token = token;
        this.user = user;
        this.username = user.getUsername();
        this.role = user.getRole().getValue();
        this.success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public AuthResponse success(boolean success) {
        this.success = success;
        return this;
    }

    public AuthResponse message(String message) {
        this.message = message;
        return this;
    }

    public AuthResponse username(String username) {
        this.username = username;
        return this;
    }

    public AuthResponse role(String role) {
        this.role = role;
        return this;
    }

    public AuthResponse build() {
        return this;
    }
}
