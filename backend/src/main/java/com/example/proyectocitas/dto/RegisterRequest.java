package com.example.proyectocitas.dto;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String role;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, String name, String role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static RegisterRequest builder() {
        return new RegisterRequest();
    }

    public RegisterRequest username(String username) {
        this.username = username;
        return this;
    }

    public RegisterRequest password(String password) {
        this.password = password;
        return this;
    }

    public RegisterRequest name(String name) {
        this.name = name;
        return this;
    }

    public RegisterRequest role(String role) {
        this.role = role;
        return this;
    }

    public RegisterRequest build() {
        return this;
    }
}
