package com.example.proyectocitas.logic;

import com.example.proyectocitas.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("service")
public class CitasService {
    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public List<Cita> buscarCitasPorPaciente(Integer idPaciente, String nombreMedico, String estado) {
        return citaRepository.buscarCitasPorPaciente(idPaciente, nombreMedico, estado);
    }
    public void saveHorario(Horario horario) {
        horarioRepository.saveAndFlush(horario);
    }
    public Paciente buscarPacientePorUsuario(String usuario) {
        return pacienteRepository.findByIdUsuario(getUsuarioByNombre(usuario));
    }
    public Medico buscarMedicoPorUsuario(String usuario) {
        return medicoRepository.findByIdUsuario(getUsuarioByNombre(usuario));
    }
    public Usuario saveUsuario(Usuario usuario) {
        Optional<Usuario> existingUsuario = usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario());

        if (existingUsuario.isPresent()) {
            throw new IllegalStateException("El usuario ya existe.");
        }

        return usuarioRepository.saveAndFlush(usuario);
    }


    public void savePaciente(Paciente paciente) {
        Optional<Paciente> existingPaciente = pacienteRepository.findById(paciente.getId());

        if (existingPaciente.isPresent()) {
            throw new IllegalStateException("El paciente ya existe.");
        }

        pacienteRepository.saveAndFlush(paciente);
    }

    public Usuario getUsuarioByNombre(String nombre) {
        return usuarioRepository.findByNombreUsuario(nombre).orElse(null);
    }


    public void saveCita(Cita cita) {
        citaRepository.saveAndFlush(cita);
    }
    public void cambiarEstadoCita(Integer idCita, String estado) {
        Cita cita = citaRepository.findById(idCita).orElse(null);
        if (cita == null) return;

        if ("Cancelada".equals(estado)) {
            horarioRepository.findById(cita.getHorarioIdHorario().getId()).ifPresent(horario -> {
                horario.setReservado(false);
                horarioRepository.saveAndFlush(horario);
            });
            citaRepository.delete(cita);
        } else {
            cita.setEstado(estado);
            citaRepository.saveAndFlush(cita);
        }
    }

    public List<Medico> findAllMedicos() {
        return medicoRepository.findAll();
    }

    public Medico buscarMedicoPorId(Integer id) {
        return medicoRepository.findById(id).orElse(null);
    }

    public Horario buscarHorarioPorId(Integer id) {
        return horarioRepository.findById(id).orElse(null);
    }

    public Medico saveMedico(Medico medico) {
        return medicoRepository.saveAndFlush(medico);
    }

    public Cita buscarCitaPorId(Integer id) {
        return citaRepository.findById(id).orElse(null);
    }

    public List<Cita> buscarCitasPorMedico(Integer idMedico, String nombrePaciente, String estado) {
        return citaRepository.buscarCitasPorMedico(idMedico, nombrePaciente, estado);
    }

    public List<Medico> buscarMedicos(String especialidad, String ubicacion) {
        return medicoRepository.buscarPorFiltros(especialidad, ubicacion).stream()
                .peek(medico -> medico.setHorarios(
                        horarioRepository.findFirst3ByIdMedicoOrderByDiaAsc(medico)
                ))
                .collect(Collectors.toList());
    }


    public List<Horario> buscarHorariosPorMedico(Integer idMedico) {
        return horarioRepository.findAllByIdMedico(buscarMedicoPorId(idMedico));
    }

    public void saveAllHorarios(List<String> diasAtencion, Medico medico) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        Map<String, DayOfWeek> mapeoDias = Map.of(
                "lunes", DayOfWeek.MONDAY,
                "martes", DayOfWeek.TUESDAY,
                "miércoles", DayOfWeek.WEDNESDAY,
                "miercoles", DayOfWeek.WEDNESDAY,
                "jueves", DayOfWeek.THURSDAY,
                "viernes", DayOfWeek.FRIDAY
        );

        Set<DayOfWeek> diasPermitidos = diasAtencion.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .map(mapeoDias::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Horario> nuevosHorarios = new ArrayList<>();
        LocalTime horaInicioDia = LocalTime.of(9, 0);
        LocalTime horaFinDia = LocalTime.of(17, 0);
        int frecuencia = medico.getFrecuenciaCitas();

        for (LocalDate fecha = inicioMes; !fecha.isAfter(finMes); fecha = fecha.plusDays(1)) {
            if (!diasPermitidos.contains(fecha.getDayOfWeek())) continue;

            for (LocalTime hora = horaInicioDia; hora.plusMinutes(frecuencia).isBefore(horaFinDia.plusMinutes(1)); hora = hora.plusMinutes(frecuencia)) {
                nuevosHorarios.add(new Horario(null, fecha, hora, hora.plusMinutes(frecuencia), medico, false));
            }
        }

        horarioRepository.saveAll(nuevosHorarios);
    }

    public List<Horario> obtenerDiasAtencion(Medico medico) {
        return horarioRepository.findAllByIdMedico(medico);
    }

}
