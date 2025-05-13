package com.example.proyectocitas.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/diagnostic")
@RequiredArgsConstructor
@Slf4j
public class DiagnosticController {

    private final RoleRepository roleRepository;
    
    @GetMapping("/init-roles")
    public ResponseEntity<?> initRoles() {
        log.info("Iniciando diagnóstico - creación de roles básicos");
        
        try {
            // Crear roles básicos
            Role adminRole = Role.builder().name("admin").build();
            Role medicoRole = Role.builder().name("medico").build();
            Role pacienteRole = Role.builder().name("paciente").build();
            
            // Guardar roles en la base de datos
            roleRepository.save(adminRole);
            roleRepository.save(medicoRole);
            roleRepository.save(pacienteRole);
            
            log.info("Roles creados con éxito");
            
            // Listar todos los roles para verificar
            return ResponseEntity.ok(roleRepository.findAll());
        } catch (Exception e) {
            log.error("Error al crear roles: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
