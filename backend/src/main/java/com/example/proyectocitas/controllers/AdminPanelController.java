package com.example.proyectocitas.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPanelController {
    
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('admin') or hasRole('medico')")
    public String adminPanel() {
        return "admin-panel";
    }
}
