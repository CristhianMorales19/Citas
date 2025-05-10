package com.example.proyectocitas.presentation.admin;

import com.example.proyectocitas.logic.CitasService;
import com.example.proyectocitas.logic.Medico;
import com.example.proyectocitas.logic.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller("admin")
@RequestMapping("/admin")
public class adminController {

    @Autowired
    private CitasService service;

    @GetMapping("/listaMedicos")
    public String listarMedicos(Model model, Principal authentication) {
        model.addAttribute("medicos", service.findAllMedicos()
                .stream()
                .filter(medico -> !medico.getAutorizado())
                .toList());

        model.addAttribute("user", authentication != null ? service.getUsuarioByNombre(authentication.getName()) : null);

        return "/presentation/admin/adminListaMedicos";
    }

    @PostMapping("/autorizar")
    public String autorizar(@RequestParam("medicoId") int medicoId) {
        Medico medico = service.buscarMedicoPorId(medicoId);
        medico.setAutorizado(true);
        service.saveMedico(medico);

        return "redirect:/admin/listaMedicos";
    }
}
