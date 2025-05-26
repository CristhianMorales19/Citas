package com.example.proyectocitas.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyectocitas.models.Patient;
import com.example.proyectocitas.models.User;
import org.springframework.data.jpa.repository.Query;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
    Optional<Patient> findByUserUsername(String username);
    
    // Compatibility method - delegates to findByUserUsername
    default Optional<Patient> findByUsuarioUsername(String username) {
        return findByUserUsername(username);
    }
}
