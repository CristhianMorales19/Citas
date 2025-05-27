package com.example.proyectocitas.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;

@Component
@Order(2) // Execute after DatabaseCleanupService
public class DataInitializer implements CommandLineRunner {
    // Explicit logger declaration since Lombok @Slf4j might not be properly processed
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Always ensure roles exist
        if (roleRepository.count() == 0) {
            initRoles();
        }
        
        // Ensure admin user exists with correct credentials
        ensureAdminUser();
        
        // Initialize other users if needed
        if (userRepository.count() <= 1) { // Only admin exists or no users
            initUsers();
        }
    }
    
    private void ensureAdminUser() {
        String adminUsername = "admin";
        String adminPassword = "admin123";
        String adminName = "Administrador";
        
        try {
            // First, handle any duplicate admin users
            List<User> duplicateAdmins = userRepository.findAllByUsername(adminUsername);
            if (!duplicateAdmins.isEmpty()) {
                // Keep the first admin and delete the rest
                User adminToKeep = duplicateAdmins.get(0);
                
                // Update the password if needed
                if (!passwordEncoder.matches(adminPassword, adminToKeep.getPassword())) {
                    adminToKeep.setPassword(passwordEncoder.encode(adminPassword));
                    adminToKeep.setName(adminName);
                    adminToKeep.setEnabled(true);
                    userRepository.save(adminToKeep);
                    log.info("Contraseña de administrador actualizada");
                }
                
                // Delete other duplicates
                if (duplicateAdmins.size() > 1) {
                    List<User> adminsToDelete = duplicateAdmins.subList(1, duplicateAdmins.size());
                    userRepository.deleteAll(adminsToDelete);
                    log.warn("Se eliminaron {} usuarios duplicados con el nombre de usuario: {}", adminsToDelete.size(), adminUsername);
                }
            } else {
                // Create new admin user if none exists
                Role adminRole = roleRepository.findByName("admin")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("admin").build()));
                
                User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name(adminName)
                    .role(adminRole)
                    .enabled(true)
                    .build();
                
                userRepository.save(admin);
                log.info("Usuario administrador creado");
            }
        } catch (Exception e) {
            log.error("Error al inicializar el usuario administrador: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private void initRoles() {
        log.info("Inicializando roles...");
        
        Role adminRole = Role.builder().name("admin").build();
        Role doctorRole = Role.builder().name("medico").build();
        Role patientRole = Role.builder().name("paciente").build();
        
        roleRepository.save(adminRole);
        roleRepository.save(doctorRole);
        roleRepository.save(patientRole);
        
        log.info("Roles inicializados");
    }
    
    private void initUsers() {
        log.info("Inicializando usuarios...");
        
        Role adminRole = roleRepository.findByName("admin")
                .orElseThrow(() -> new RuntimeException("Rol de administrador no encontrado"));
        
        Role doctorRole = roleRepository.findByName("medico")
                .orElseThrow(() -> new RuntimeException("Rol de médico no encontrado"));
        
        Role patientRole = roleRepository.findByName("paciente")
                .orElseThrow(() -> new RuntimeException("Rol de paciente no encontrado"));
        
        // Admin user
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .name("Administrador")
                .role(adminRole)
                .enabled(true)
                .build();
        
        // Doctor user
        User doctor = User.builder()
                .username("doctor")
                .password(passwordEncoder.encode("doctor123"))
                .name("Dr. Juan Pérez")
                .role(doctorRole)
                .enabled(true)
                .build();
        
        // Patient user
        User patient = User.builder()
                .username("paciente")
                .password(passwordEncoder.encode("paciente123"))
                .name("María García")
                .role(patientRole)
                .enabled(true)
                .build();
        
        userRepository.save(admin);
        userRepository.save(doctor);
        userRepository.save(patient);
        
        log.info("Usuarios inicializados");
    }
}
