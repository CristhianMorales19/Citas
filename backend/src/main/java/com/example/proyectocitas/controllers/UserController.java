package com.example.proyectocitas.controllers;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.services.UserRegistrationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class UserController {
    
    private final UserRegistrationService userRegistrationService;
    
    @PostMapping("/register-alternative")
    public ResponseEntity<?> registerAlternative(
            @Valid @RequestBody RegisterRequest request, 
            BindingResult bindingResult) {
        
        log.info("Iniciando registro alternativo para usuario: {}", request.getUsername());
        
        // Validar campos obligatorios
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : ""
                ));
                
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error de validación",
                "errors", errors
            ));
        }

        try {
            Map<String, Object> result = userRegistrationService.registerUser(request);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error en el registro alternativo: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error al registrar el usuario: " + e.getMessage()
            ));
        }
    }
}
