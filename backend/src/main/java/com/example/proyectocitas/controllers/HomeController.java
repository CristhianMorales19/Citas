package com.example.proyectocitas.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.example.proyectocitas.models.AppUser;
import com.example.proyectocitas.repositories.UserRepository;

@RestController
public class HomeController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/")
    public String home() {
        return "Bienvenido a la API de Proyecto Citas";
    }

    @GetMapping("/test-db")
    public ResponseEntity<String> testDatabase() {
        try {
            // Try to fetch users to verify connection
            List<AppUser> users = userRepository.findAll();
            return ResponseEntity.ok("Database connection successful! Found " + users.size() + " users.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database connection error: " + e.getMessage());
        }
    }
}
