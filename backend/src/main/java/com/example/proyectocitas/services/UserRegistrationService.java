package com.example.proyectocitas.services;

import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public Map<String, Object> registerUser(RegisterRequest request) {
        // Validar si el usuario ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // 1. Crear y guardar el usuario en una transacción separada
        User savedUser = createUserTransactionally(request);
        
        // 2. Si es médico, crear el perfil en una transacción separada
        if ("medico".equalsIgnoreCase(request.getRole())) {
            try {
                createDoctorProfileTransactionally(savedUser);
            } catch (Exception e) {
                log.error("Error creando perfil de médico: {}", e.getMessage(), e);
                // Continuamos con el registro aunque falle la creación del perfil
            }
        }

        // 3. Generar token JWT
        String token = jwtService.generateToken(savedUser);
        
        return Map.of(
            "success", true,
            "message", "Usuario registrado exitosamente",
            "token", token,
            "user", Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "name", savedUser.getName(),
                "role", savedUser.getRole().getName()
            )
        );
    }

    @Transactional(transactionManager = "transactionManager")
    protected User createUserTransactionally(RegisterRequest request) {
        String roleName = "medico".equalsIgnoreCase(request.getRole()) ? "medico" : "paciente";
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
        
        return userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void createDoctorProfileTransactionally(User user) {
        if (doctorRepository.findByUser(user).isEmpty()) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
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
            log.info("Perfil de médico creado para: {}", user.getUsername());
        }
    }
}
