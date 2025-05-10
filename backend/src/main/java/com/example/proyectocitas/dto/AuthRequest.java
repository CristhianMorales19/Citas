package com.example.proyectocitas.dto;

public class AuthRequest {
    private String username;
    private String password;

    public AuthRequest() {
    }

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static AuthRequest builder() {
        return new AuthRequest();
    }

    public AuthRequest username(String username) {
        this.username = username;
        return this;
    }

    public AuthRequest password(String password) {
        this.password = password;
        return this;
    }

    public AuthRequest build() {
        return this;
    }
}
