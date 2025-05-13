package com.example.proyectocitas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.AuthResponse;
import com.example.proyectocitas.dto.LoginRequest;
import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.dto.UserDTO;
import com.example.proyectocitas.services.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error en los logs
            return ResponseEntity.status(500).body(
                AuthResponse.builder()
                    .success(false)
                    .message("Error al registrar el usuario: " + e.getMessage())
                    .build()
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Solicitud de inicio de sesión recibida para usuario: {}", request.getNombreUsuario());
        try {
            AuthResponse response = authService.login(request);
            log.info("Inicio de sesión exitoso para usuario: {}", request.getNombreUsuario());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en el inicio de sesión para usuario {}: {}", request.getNombreUsuario(), e.getMessage(), e);
            return ResponseEntity.status(401).body(
                AuthResponse.builder()
                    .success(false)
                    .message("Error en la autenticación: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }
}
