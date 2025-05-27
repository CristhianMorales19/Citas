package com.example.proyectocitas.services;

import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.exceptions.UserAlreadyExistsException;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserRegistrationService(UserRepository userRepository, 
                                 RoleRepository roleRepository,
                                 DoctorRepository doctorRepository, 
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está en uso");
        }

        User savedUser = createUserTransactionally(request);
        
        if ("medico".equalsIgnoreCase(request.getRole())) {
            try {
                createDoctorProfileTransactionally(savedUser);
            } catch (Exception e) {
                log.error("Error al crear perfil de médico para usuario {}: {}", 
                    savedUser.getUsername(), e.getMessage(), e);
            }
        }

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
            Doctor doctor = Doctor.builder()
                .user(user)
                .especialidad("General")
                .costoConsulta(0.0)
                .appointmentDuration(30)
                .activo(true)
                .status("PENDING")
                .profileConfigured(false)
                .calificacion(0.0)
                .descripcion("")
                .location("")
                .presentation("")
                .photoUrl("")
                .build();
            
            doctorRepository.save(doctor);
            log.info("Perfil de médico creado exitosamente para: {}", user.getUsername());
        }
    }
}