package com.example.proyectocitas.presentation.medicos;

import com.example.proyectocitas.logic.CitasService;
import com.example.proyectocitas.logic.Horario;
import com.example.proyectocitas.logic.Medico;
import com.example.proyectocitas.logic.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Controller("medicos")
@RequestMapping("/medicos")
public class MedicosController {

    @Autowired
    private CitasService service;

    @GetMapping("/listaMedicos")
    public String listaMedicos(@RequestParam(value = "especialidad", required = false) String especialidad,
                               @RequestParam(value = "ubicacion", required = false) String ubicacion,
                               Model model, Principal authentication) {
        model.addAttribute("medicos", service.buscarMedicos(especialidad, ubicacion));
        setUserModel(authentication, model);
        return "/presentation/medicos/listaMedicos";
    }

    @PostMapping("/schedule")
    public String schedule(@RequestParam Integer medicoId, @RequestParam Integer page, Model model, Principal authentication) {
        Medico medico = service.buscarMedicoPorId(medicoId);
        List<Horario> horarios = getPaginatedHorarios(medicoId, page);

        model.addAttribute("medico", medico);
        model.addAttribute("horarios", horarios);
        model.addAttribute("page", page);
        setUserModel(authentication, model);

        return "presentation/medicos/schedule";
    }

    @PostMapping("/next")
    public String next(@RequestParam Integer medicoId, @RequestParam Integer page, Model model, Principal authentication) {
        return schedule(medicoId, page + 1, model, authentication);
    }

    @PostMapping("/back")
    public String back(@RequestParam Integer medicoId, @RequestParam Integer page, Model model, Principal authentication) {
        return schedule(medicoId, page - 1, model, authentication);
    }

    @GetMapping("/medicoPerfil")
    public String medicoPerfil(Principal principal, Model model) {
        if (principal != null) {
            Medico medico = service.buscarMedicoPorUsuario(principal.getName());
            model.addAttribute("medico", medico);

            Set<String> diasAtencionSeleccionados = getDiasAtencion(medico);
            model.addAttribute("diasAtencionSeleccionados", diasAtencionSeleccionados);

            setUserModel(principal, model);
            return "presentation/medicos/perfil";
        }
        model.addAttribute("user", null);
        return "redirect:/login";
    }

    @GetMapping("/medicoListaCitas")
    public String medicoListaCitas(Principal principal, Model model,
                                   @RequestParam(value = "nombrePaciente", required = false) String nombrePaciente,
                                   @RequestParam(value = "estado", required = false) String estado) {
        if (principal != null) {
            Medico medico = service.buscarMedicoPorUsuario(principal.getName());
            model.addAttribute("citas", service.buscarCitasPorMedico(medico.getId(), nombrePaciente, estado));
            model.addAttribute("nombreMedico", medico.getNombre());
            setUserModel(principal, model);

            return "/presentation/medicos/ListaCitas";
        }
        model.addAttribute("user", null);
        return "redirect:/login";
    }

    @PostMapping("/medico/editar")
    public String guardar(@ModelAttribute("medico") Medico medico,
                          @RequestParam(value = "foto", required = false) MultipartFile foto,
                          @RequestParam(value = "diasAtencion", required = false) List<String> diasAtencion) throws IOException {
        String rutaImagen = saveFoto(medico, foto);
        service.saveMedico(medico);

        if (diasAtencion != null && !diasAtencion.isEmpty()) {
            service.saveAllHorarios(diasAtencion, medico);
        }

        return "redirect:/medicos/medicoPerfil";
    }

    private void setUserModel(Principal authentication, Model model) {
        if (authentication != null) {
            Usuario user = service.getUsuarioByNombre(authentication.getName());
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", null);
        }
    }

    private List<Horario> getPaginatedHorarios(Integer medicoId, Integer page) {
        List<Horario> horarios = service.buscarHorariosPorMedico(medicoId)
                .stream()
                .sorted(Comparator.comparing(Horario::getDia))
                .collect(Collectors.toList());

        int fromIndex = (page - 1) * 2;
        return fromIndex < horarios.size() ? horarios.subList(fromIndex, Math.min(fromIndex + 2, horarios.size())) : Collections.emptyList();
    }

    private Set<String> getDiasAtencion(Medico medico) {
        List<Horario> horarios = service.obtenerDiasAtencion(medico);
        Set<String> diasAtencionSeleccionados = new HashSet<>();

        for (Horario horario : horarios) {
            DayOfWeek dayOfWeek = horario.getDia().getDayOfWeek();
            switch (dayOfWeek) {
                case MONDAY: diasAtencionSeleccionados.add("lunes"); break;
                case TUESDAY: diasAtencionSeleccionados.add("martes"); break;
                case WEDNESDAY: diasAtencionSeleccionados.add("miércoles"); break;
                case THURSDAY: diasAtencionSeleccionados.add("jueves"); break;
                case FRIDAY: diasAtencionSeleccionados.add("viernes"); break;
            }
        }
        return diasAtencionSeleccionados;
    }

    private String saveFoto(Medico medico, MultipartFile foto) throws IOException {
        String directorioBase = "C:/ImagenesProyecto1/";
        File directorio = new File(directorioBase);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String rutaImagen = directorioBase + medico.getId() + ".jpg";
        if (foto != null && !foto.isEmpty()) {
            foto.transferTo(new File(rutaImagen));
        }
        return rutaImagen;
    }
}
