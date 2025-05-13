package com.example.proyectocitas.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.services.DoctorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_admin')")
@RequiredArgsConstructor
public class AdminController {

    private final DoctorService doctorService;
    
    @GetMapping("/medicos/pendientes")
    public ResponseEntity<List<DoctorDTO>> getPendingDoctors() {
        return ResponseEntity.ok(doctorService.getPendingDoctors());
    }
    
    @PostMapping("/medicos/{id}/aprobar")
    public ResponseEntity<DoctorDTO> approveDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.approveDoctor(id));
    }
    
    @PostMapping("/medicos/{id}/rechazar")
    public ResponseEntity<DoctorDTO> rejectDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.rejectDoctor(id));
    }
}
