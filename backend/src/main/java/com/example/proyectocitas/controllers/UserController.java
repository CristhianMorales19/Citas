package com.example.proyectocitas.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.security.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @PostMapping("/register-simple")
    public ResponseEntity<?> registerSimple(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        log.info("Iniciando registro simple para usuario: {}", request.getUsername());
        
        // Validar campos obligatorios
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error de validación",
                "errors", errors
            ));
        }
        
        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "El nombre de usuario ya está en uso"
            ));
        }

        try {
            // 1. Crear y guardar el usuario
            String roleName = "medico".equals(request.getRole()) ? "medico" : "paciente";
            Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setEnabled(true);
            user.setRole(role);
            
            User savedUser = userRepository.save(user);
            log.info("Usuario creado exitosamente: {}", savedUser.getUsername());

            // 2. Si es médico, crear perfil
            if ("medico".equals(roleName)) {
                try {
                    if (doctorRepository.findByUser(savedUser).isEmpty()) {
                        Doctor doctor = new Doctor();
                        doctor.setUser(savedUser);
                        doctor.setEspecialidad("General");
                        doctor.setCostoConsulta(0.0);
                        doctor.setAppointmentDuration(30);
                        doctor.setActivo(true);
                        doctor.setStatus("PENDING");
                        doctor.setProfileConfigured(false);
                        doctor.setCalificacion(0.0);
                        doctor.setDescripcion("");
                        doctor.setLocation("");
                        doctor.setPresentation("");
                        doctor.setPhotoUrl("");
                        doctor.setHorarios(new java.util.ArrayList<>());

                        doctorRepository.save(doctor);
                        log.info("Perfil de médico creado para: {}", savedUser.getUsername());
                    }
                } catch (Exception e) {
                    log.error("Error al crear perfil de médico: {}", e.getMessage(), e);
                    // Continuar aunque falle la creación del perfil
                }
            }

            // 3. Generar token JWT
            String token = jwtService.generateToken(savedUser);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario registrado exitosamente",
                "token", token,
                "user", Map.of(
                    "id", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "name", savedUser.getName(),
                    "role", savedUser.getRole().getName()
                )
            ));
            
        } catch (Exception e) {
            log.error("Error en el registro: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error al registrar el usuario: " + e.getMessage()
            ));
        }
    }
}
