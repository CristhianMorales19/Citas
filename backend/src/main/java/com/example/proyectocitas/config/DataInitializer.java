package com.example.proyectocitas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            initRoles();
        }
        
        if (userRepository.count() == 0) {
            initUsers();
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
