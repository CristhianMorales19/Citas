package com.example.proyectocitas.presentation.citas;

import com.example.proyectocitas.logic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller("citas")
@RequestMapping("/citas")
public class CitasController {

    @Autowired
    private CitasService service;

    @PostMapping("/paginaConfirmarCita")
    public String paginaConfirmarCita(Model model, @RequestParam Integer idHorario,
                                      @RequestParam Integer idMedico, Principal authentication) {
        model.addAttribute("medico", service.buscarMedicoPorId(idMedico));
        model.addAttribute("horario", service.buscarHorarioPorId(idHorario));
        model.addAttribute("user", authentication != null ? service.getUsuarioByNombre(authentication.getName()) : null);
        return "/presentation/citas/ConfirmarCita";
    }

    @PostMapping("/confirmarCita")
    public String confirmarCita(Principal principal, @RequestParam("idHorario") Integer idHorario, @RequestParam("idMedico") Integer idMedico) {
        if (principal != null) {
            Paciente paciente = service.buscarPacientePorUsuario(principal.getName());
            if (paciente != null) {
                Medico medico = service.buscarMedicoPorId(idMedico);
                Horario horario = service.buscarHorarioPorId(idHorario);

                Cita cita = new Cita();
                cita.setHorarioIdHorario(horario);
                cita.setIdMedico(medico);
                cita.setIdPaciente(paciente);
                cita.setEstado("Pendiente");

                horario.setReservado(true);
                service.saveHorario(horario);

                service.saveCita(cita);
                return "redirect:/pacientes/HistoricoCitas";
            }
        }
        return "redirect:/citas/ConfirmarCita";
    }

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/medicos/listaMedicos";
    }

    @PostMapping({"/CancelarCita", "/CompletarCita"})
    public String actualizarEstadoCita(@RequestParam Integer idCita, @RequestParam String action) {
        String nuevoEstado = "CancelarCita".equals(action) ? "Cancelada" : "Completada";
        service.cambiarEstadoCita(idCita, nuevoEstado);
        return "redirect:" + ("CancelarCita".equals(action) ? "/pacientes/HistoricoCitas" : "/medicos/medicoListaCitas");
    }
}
