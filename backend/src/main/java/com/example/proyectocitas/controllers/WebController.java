package com.example.proyectocitas.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.services.DoctorService;
import com.example.proyectocitas.services.UserService;

@Controller
public class WebController {

    private final DoctorService doctorService;
    private final UserService userService;

    public WebController(DoctorService doctorService, UserService userService) {
        this.doctorService = doctorService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/admin-panel")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel(Model model) {
        System.out.println("=== DEBUG: Cargando panel de administración ===");
        
        List<DoctorDTO> pendingDoctors = doctorService.getPendingDoctors();
        System.out.println("DEBUG: Médicos pendientes encontrados: " + pendingDoctors.size());
        
        for (DoctorDTO doctor : pendingDoctors) {
            System.out.println("DEBUG: Doctor pendiente - ID: " + doctor.getId() + 
                             ", Nombre: " + doctor.getName() + 
                             ", Especialidad: " + doctor.getSpecialty());
        }
        
        model.addAttribute("pendingDoctors", pendingDoctors);
        
        // Obtener estadísticas para el panel de control
        Long totalDoctors = doctorService.getTotalDoctorsCount();
        Long totalPatients = userService.getTotalPatientsCount();
        
        System.out.println("DEBUG: Total médicos: " + totalDoctors);
        System.out.println("DEBUG: Total pacientes: " + totalPatients);
        
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalAppointments", 0); // Puedes agregarlo si tienes un servicio para citas
        
        System.out.println("=== DEBUG: Panel de administración cargado ===");
        return "admin-panel";
    }

    @GetMapping("/admin/approve-doctor/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveDoctor(@PathVariable Long id) {
        doctorService.approveDoctor(id);
        return "redirect:/admin-panel";
    }
    
    @GetMapping("/admin/reject-doctor/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectDoctor(@PathVariable Long id) {
        doctorService.rejectDoctor(id);
        return "redirect:/admin-panel";
    }

    @GetMapping("/admin-panel/status-debug")
    public String adminPanelStatusDebug(Model model) {
        model.addAttribute("statusCounts", doctorService.getStatusCounts());
        return "admin-panel-status-debug";
    }
}
