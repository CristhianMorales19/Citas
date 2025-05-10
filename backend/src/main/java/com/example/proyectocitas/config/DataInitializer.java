package com.example.proyectocitas.config;

import com.example.proyectocitas.models.AppUser;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Check if there are any users in the database
        if (userRepository.count() == 0) {
            // Create test users
            AppUser doctor = new AppUser(
                "doctor1",
                passwordEncoder.encode("doctor123"),
                "Dr. Juan Perez",
                Role.MEDICO
            );
            
            AppUser patient = new AppUser(
                "patient1",
                passwordEncoder.encode("patient123"),
                "Maria Lopez",
                Role.PACIENTE
            );
            
            userRepository.save(doctor);
            userRepository.save(patient);
            
            System.out.println("Test users created:");
            System.out.println("Doctor: username=doctor1, password=doctor123");
            System.out.println("Patient: username=patient1, password=patient123");
        } else {
            System.out.println("Database already has users");
        }
    }
}
