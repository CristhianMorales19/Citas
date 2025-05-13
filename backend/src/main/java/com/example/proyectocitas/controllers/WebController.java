package com.example.proyectocitas.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.services.DoctorService;
import com.example.proyectocitas.services.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final DoctorService doctorService;
    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/admin-panel")
    @PreAuthorize("hasRole('ROLE_admin')")
    public String adminPanel(Model model) {
        List<DoctorDTO> pendingDoctors = doctorService.getPendingDoctors();
        model.addAttribute("pendingDoctors", pendingDoctors);
        
        // Obtener estadísticas para el panel de control
        model.addAttribute("totalDoctors", doctorService.getTotalDoctorsCount());
        model.addAttribute("totalPatients", userService.getTotalPatientsCount());
        model.addAttribute("totalAppointments", 0); // Puedes agregarlo si tienes un servicio para citas
        
        return "admin-panel";
    }
    
    @GetMapping("/admin/approve-doctor/{id}")
    @PreAuthorize("hasRole('ROLE_admin')")
    public String approveDoctor(@PathVariable Long id) {
        doctorService.approveDoctor(id);
        return "redirect:/admin-panel";
    }
    
    @GetMapping("/admin/reject-doctor/{id}")
    @PreAuthorize("hasRole('ROLE_admin')")
    public String rejectDoctor(@PathVariable Long id) {
        doctorService.rejectDoctor(id);
        return "redirect:/admin-panel";
    }
}
