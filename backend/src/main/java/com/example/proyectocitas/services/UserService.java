package com.example.proyectocitas.services;

import org.springframework.stereotype.Service;

import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    public Long getTotalPatientsCount() {
        Role patientRole = roleRepository.findByName("paciente")
                .orElseThrow(() -> new RuntimeException("Rol de paciente no encontrado"));
        
        return userRepository.countByRole(patientRole);
    }
}
