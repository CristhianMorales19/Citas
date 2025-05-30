package com.example.proyectocitas.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.AppointmentDTO;
import com.example.proyectocitas.services.AppointmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pacientes")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    
    @GetMapping("/citas")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(userDetails.getUsername()));
    }
}
