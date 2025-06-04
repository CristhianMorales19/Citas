package com.example.proyectocitas.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Patient;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.PatientRepository;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@RequiredArgsConstructor
//@Slf4j // No usar Slf4j, ya que se define log manualmente
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register-alternative")
    @Transactional
    public ResponseEntity<?> registerAlternative(@RequestBody RegisterRequest request) {
        log.info("Iniciando registro alternativo para usuario: {}", request.getUsername());
        try {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Las contraseñas no coinciden"
                ));
            }
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "El nombre de usuario ya está en uso"
                ));
            }
            String roleName = request.getRole() != null ? request.getRole() : "paciente";
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
            if ("medico".equalsIgnoreCase(roleName)) {
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
                        doctorRepository.save(doctor);
                        log.info("Perfil de médico creado para: {}", savedUser.getUsername());
                    }
                } catch (Exception e) {
                    log.error("Error al crear perfil de médico: {}", e.getMessage(), e);
                }
            } else if ("paciente".equalsIgnoreCase(roleName) || roleName == null) {
                log.info("Usuario será registrado como paciente: {}", savedUser.getUsername());
                try {
                    log.info("Verificando si ya existe paciente para usuario: {}", savedUser.getUsername());
                    if (patientRepository.findByUser(savedUser).isEmpty()) {
                        log.info("No existe paciente, creando nuevo paciente para: {}", savedUser.getUsername());
                        Patient patient = new Patient();
                        patient.setUser(savedUser);
                        patient.setNombre(savedUser.getName());
                        patient.setMedicalHistory("");
                        patient.setAllergies("");
                        patient.setContactInformation("");
                        patientRepository.save(patient);
                        log.info("Perfil de paciente creado exitosamente para: {}", savedUser.getUsername());
                    } else {
                        log.warn("Ya existe un paciente para el usuario: {}", savedUser.getUsername());
                    }
                } catch (Exception e) {
                    log.error("Error al crear perfil de paciente para {}: {}", savedUser.getUsername(), e.getMessage(), e);
                }
            }
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
            log.error("Error en el registro alternativo: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error al registrar el usuario: " + e.getMessage()
            ));
        }
    }
}
