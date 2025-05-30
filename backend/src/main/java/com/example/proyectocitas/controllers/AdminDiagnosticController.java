package com.example.proyectocitas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/diagnostic")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
public class AdminDiagnosticController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDiagnosticController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin-user")
    public ResponseEntity<Map<String, Object>> checkAdminUser() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> adminUserOpt = userRepository.findByUsername("admin");
            
            if (adminUserOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario admin no encontrado");
                return ResponseEntity.ok(response);
            }
            
            User adminUser = adminUserOpt.get();
            response.put("success", true);
            response.put("adminExists", true);
            response.put("adminId", adminUser.getId());
            response.put("adminUsername", adminUser.getUsername());
            response.put("adminName", adminUser.getName());
            response.put("adminEnabled", adminUser.isEnabled());
            response.put("adminRole", adminUser.getRole() != null ? adminUser.getRole().getName() : "Sin rol");
            response.put("adminRoleId", adminUser.getRole() != null ? adminUser.getRole().getId() : null);
            
            // Verificar la contraseña
            boolean passwordMatches = passwordEncoder.matches("admin123", adminUser.getPassword());
            response.put("passwordMatches", passwordMatches);
            
            // Verificar las authorities
            response.put("authorities", adminUser.getAuthorities().toString());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar usuario admin: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }
}
