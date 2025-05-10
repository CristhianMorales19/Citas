package com.example.proyectocitas.presentation.pacientes;

import com.example.proyectocitas.logic.CitasService;
import com.example.proyectocitas.logic.Paciente;
import com.example.proyectocitas.logic.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller("pacientes")
@RequestMapping("/pacientes")
public class PacientesController {

    @Autowired
    private CitasService service;

    @GetMapping("/HistoricoCitas")
    public String historicoCitas(Principal principal,
                                 Model model,
                                 @RequestParam(value = "nombreMedico", required = false) String nombreMedico,
                                 @RequestParam(value = "estado", required = false) String estado) {

        if (principal != null) {
            Paciente paciente = service.buscarPacientePorUsuario(principal.getName());
            model.addAttribute("citas", service.buscarCitasPorPaciente(paciente.getId(), nombreMedico, estado));
            model.addAttribute("nombrePaciente", paciente.getNombre());
            Usuario user = service.getUsuarioByNombre(principal.getName());
            model.addAttribute("user", user);
            return "/presentation/pacientes/HistoricoCitas";
        } else {
            model.addAttribute("user", null);
            return "redirect:/login";
        }
    }
}
