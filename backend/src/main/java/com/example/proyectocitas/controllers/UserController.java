package com.example.proyectocitas.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.listeners.UserRegistrationListener;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRegistrationListener userRegistrationListener;
    
    @PostMapping("/register-alternative")
    @Transactional
    public ResponseEntity<?> registerAlternative(@RequestBody RegisterRequest request) {
        log.info("Iniciando registro alternativo para usuario: {}", request.getUsername());
        
        try {
            // Verificar si el usuario ya existe
            if (userRepository.existsByUsername(request.getUsername())) {
                log.warn("Usuario ya existe: {}", request.getUsername());
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El nombre de usuario ya está en uso"
                ));
            }
            
            // Obtener el rol enviado desde el frontend o usar 'paciente' por defecto
            String requestedRole = (request.getRole() != null) ? request.getRole() : "paciente";
            log.info("Rol solicitado: {}", requestedRole);
            
            // Verificar que el rol sea válido (paciente o medico)
            final String roleName;
            if (!"paciente".equals(requestedRole) && !"medico".equals(requestedRole)) {
                log.warn("Rol inválido solicitado: {}, usando 'paciente' por defecto", requestedRole);
                roleName = "paciente";
            } else {
                roleName = requestedRole;
            }
            
            // Buscar o crear el rol si no existe
            Role role;
            try {
                role = roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        log.info("Creando rol: {}", roleName);
                        Role newRole = new Role();
                        newRole.setName(roleName);
                        return roleRepository.save(newRole);
                    });
                
                log.info("Rol encontrado/creado: {}", role.getName());
            } catch (Exception e) {
                log.error("Error al buscar/crear rol: {}", e.getMessage());
                
                // Crear el rol directamente
                role = new Role();
                role.setName(roleName);
                role = roleRepository.save(role);
                log.info("Rol creado manualmente: {}", role.getName());
            }
            
            // Crear el usuario con el rol
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setEnabled(true);
            user.setRole(role);
            
            // Guardar usuario
            user = userRepository.save(user);
            log.info("Usuario guardado correctamente: {}", user.getUsername());
            
            // Si el usuario es de tipo médico, crear el perfil de médico automáticamente
            if ("medico".equals(role.getName())) {
                log.info("El usuario registrado es un médico, creando perfil de médico pendiente");
                userRegistrationListener.createDoctorProfileIfNeeded(user);
            }
            
            // Generar token JWT
            String token = jwtService.generateToken(user);
            
            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario registrado correctamente");
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "name", user.getName(),
                "role", user.getRole().getName()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error durante el registro alternativo: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error durante el registro: " + e.getMessage(),
                "error", e.getClass().getSimpleName()
            ));
        }
    }
}
