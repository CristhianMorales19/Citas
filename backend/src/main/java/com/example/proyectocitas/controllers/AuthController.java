package com.example.proyectocitas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.proyectocitas.services.AuthService;
import com.example.proyectocitas.dto.AuthRequest;
import com.example.proyectocitas.dto.AuthResponse;
import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.models.AppUser;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            response.setSuccess(true);
            response.setMessage("Registro exitoso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            response.setSuccess(true);
            response.setMessage("Inicio de sesión exitoso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AuthResponse errorResponse = new AuthResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/test-users")
    public ResponseEntity<List<AppUser>> testUsers() {
        List<AppUser> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<AppUser> getUser(@PathVariable String username) {
        Optional<AppUser> user = userRepository.findByUsername(username);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
