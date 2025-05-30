package com.example.proyectocitas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.proyectocitas.dto.AppointmentDTO;
import com.example.proyectocitas.dto.AppointmentRequest;
import com.example.proyectocitas.dto.HorarioDTO;
import com.example.proyectocitas.dto.ScheduleRequest;
import com.example.proyectocitas.exceptions.AppointmentNotAvailableException;
import com.example.proyectocitas.exceptions.DoctorNotFoundException;
import com.example.proyectocitas.exceptions.HorarioNotFoundException;
import com.example.proyectocitas.exceptions.PatientNotFoundException;
import com.example.proyectocitas.exceptions.ResourceNotFoundException;
import com.example.proyectocitas.models.*;
import com.example.proyectocitas.repositories.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio para gestionar las citas médicas
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {
    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final HorarioRepository horarioRepository;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int WEEKS_TO_GENERATE = 4; // Generar citas para 4 semanas
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Obtiene todas las citas del sistema
     */
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una cita por su ID
     */
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        return convertToDTO(appointment);
    }

    /**
     * Obtiene las citas de un médico
     */
    public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
                
        return appointmentRepository.findByMedico(doctor).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas de un médico por estado
     */
    public List<AppointmentDTO> getAppointmentsByDoctorAndStatus(Long doctorId, Appointment.Status status) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
                
        return appointmentRepository.findByMedicoAndEstado(doctor, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas de un paciente
     */
    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con ID: " + patientId));
                
        return appointmentRepository.findByPaciente(patient).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas de un paciente por estado
     */
    public List<AppointmentDTO> getAppointmentsByPatientAndStatus(Long patientId, Appointment.Status status) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con ID: " + patientId));
                
        return appointmentRepository.findByPacienteAndEstado(patient, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas de un médico para una fecha específica
     */
    public List<AppointmentDTO> getDoctorAppointmentsForDate(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
                
        return appointmentRepository.findByDoctorAndDate(doctor, date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un horario está disponible para un médico en una fecha y hora específicas
     */
    public boolean isTimeSlotAvailable(Long doctorId, LocalDate date, LocalTime time) {
        return !appointmentRepository.existsByDoctorIdAndDateAndTime(doctorId, date, time);
    }
    
    /**
     * Genera bloques de citas disponibles para un médico según su horario
     */
    @Transactional
    public void generateAppointmentSlots(Long doctorId, ScheduleRequest scheduleRequest, int weeksInAdvance) {
        System.out.println("Iniciando generación de citas para el doctor ID: " + doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
        
        LocalDate startDate = scheduleRequest.getFechaInicio() != null ? 
                scheduleRequest.getFechaInicio() : LocalDate.now();
        LocalDate endDate = scheduleRequest.getFechaFin() != null ? 
                scheduleRequest.getFechaFin() : startDate.plusWeeks(weeksInAdvance);
        
        System.out.println("Rango de fechas: " + startDate + " a " + endDate);
        
        // Validar fechas
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        // Validar que el horario tenga los datos necesarios
        if (scheduleRequest.getHoraInicio() == null || scheduleRequest.getHoraFin() == null) {
            throw new IllegalArgumentException("El horario debe tener una hora de inicio y fin definidas");
        }
        
        // Generar citas para cada día en el rango
        LocalDate currentDate = startDate;
        int citasGeneradas = 0;
        
        while (!currentDate.isAfter(endDate)) {
            // Verificar si el día de la semana coincide con el día del horario
            if (currentDate.getDayOfWeek() == scheduleRequest.getDiaSemana()) {
                // Verificar si el día de la semana está en los días disponibles (si se especificaron)
                if (scheduleRequest.getDiasDisponibles() == null || 
                    scheduleRequest.getDiasDisponibles().contains(currentDate.getDayOfWeek())) {
                    System.out.println("Generando citas para el día: " + currentDate);
                    citasGeneradas += generateAppointmentsForDay(doctor, currentDate, scheduleRequest);
                } else {
                    System.out.println("Día no programado: " + currentDate);
                }
            } else {
                System.out.println("Día no programado: " + currentDate);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        System.out.println("Total de citas generadas: " + citasGeneradas);
    }
    
    private int generateAppointmentsForDay(Doctor doctor, LocalDate date, ScheduleRequest scheduleRequest) {
        LocalTime startTime = scheduleRequest.getHoraInicio();
        LocalTime endTime = scheduleRequest.getHoraFin();
        int duration = scheduleRequest.getDuracionCita() != null ? 
                scheduleRequest.getDuracionCita() : 30; // 30 minutos por defecto
        
        LocalTime currentTime = startTime;
        int citasCreadas = 0;
        
        System.out.println("Generando citas de " + startTime + " a " + endTime + " con duración " + duration + " minutos");
        
        while (!currentTime.isAfter(endTime) && 
               currentTime.plusMinutes(duration).isBefore(endTime.plusMinutes(1))) {
            // Verificar si ya existe una cita a esta hora
            if (!appointmentRepository.existsByMedicoAndFechaAndHoraInicio(doctor, date, currentTime)) {
                // Crear nueva cita disponible
                Appointment appointment = new Appointment();
                appointment.setMedico(doctor);
                appointment.setFecha(date);
                appointment.setHoraInicio(currentTime);
                appointment.setHoraFin(currentTime.plusMinutes(duration));
                appointment.setEstado(Appointment.Status.DISPONIBLE);
                appointment.setFechaCreacion(LocalDateTime.now());
                appointment.setFechaActualizacion(LocalDateTime.now());
                
                appointmentRepository.save(appointment);
                citasCreadas++;
                
                System.out.println("Cita creada: " + date + " a las " + currentTime);
            } else {
                System.out.println("Cita ya existe: " + date + " a las " + currentTime);
            }
            
            // Avanzar al siguiente bloque de tiempo
            currentTime = currentTime.plusMinutes(duration);
        }
        
        return citasCreadas;
    }
    
    /**
     * Programa una cita disponible para un paciente
     */
    @Transactional
    public AppointmentDTO scheduleAppointment(String username, Long appointmentId) {
        // Obtener la cita
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Verificar que la cita esté disponible
        if (appointment.getStatus() != Appointment.Status.DISPONIBLE) {
            throw new AppointmentNotAvailableException("La cita no está disponible para ser agendada");
        }
        
        // Obtener el paciente
        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado"));
        
        // Actualizar la cita
        appointment.setPatient(patient);
        appointment.setStatus(Appointment.Status.AGENDADA);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        return convertToDTO(appointmentRepository.save(appointment));
    }
    
    /**
     * Actualiza el estado de una cita
     */
    @Transactional
    public AppointmentDTO updateAppointmentStatus(Long appointmentId, Appointment.Status newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Validar transición de estado
        if (!isValidStatusTransition(appointment.getStatus(), newStatus)) {
            throw new IllegalStateException("Transición de estado no permitida: " + 
                    appointment.getStatus() + " -> " + newStatus);
        }
        
        appointment.setStatus(newStatus);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        return convertToDTO(appointmentRepository.save(appointment));
    }
    
    private boolean isValidStatusTransition(Appointment.Status currentStatus, Appointment.Status newStatus) {
        // Lógica para validar transiciones de estado permitidas
        switch (currentStatus) {
            case DISPONIBLE: // AVAILABLE
                return newStatus == Appointment.Status.AGENDADA || 
                       newStatus == Appointment.Status.CANCELADA;
            case AGENDADA: // BOOKED 
                return newStatus == Appointment.Status.CONFIRMADA || 
                       newStatus == Appointment.Status.CANCELADA;
            case CONFIRMADA: // CONFIRMED
                return newStatus == Appointment.Status.EN_PROCESO || 
                       newStatus == Appointment.Status.CANCELADA;
            case EN_PROCESO: // IN_PROGRESS
                return newStatus == Appointment.Status.COMPLETADA || 
                       newStatus == Appointment.Status.CANCELADA;
            case COMPLETADA: // COMPLETED
            case CANCELADA: // CANCELLED
            case NO_ASISTIO: // NO_SHOW
                return false; // Estados finales
            default:
                return false;
        }
    }
    
    /**
     * Cancela una cita
     */
    @Transactional
    public void cancelAppointment(Long appointmentId, String username, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + appointmentId));
        
        // Verificar permisos (solo el paciente o el médico pueden cancelar)
        boolean isPatient = patientRepository.findByUserUsername(username).isPresent();
        boolean isDoctor = doctorRepository.findByUserUsername(username).isPresent();
        
        if (!isPatient && !isDoctor) {
            throw new SecurityException("No tiene permiso para cancelar esta cita");
        }
        
        // Actualizar estado de la cita
        appointment.setStatus(Appointment.Status.CANCELADA);
        appointment.setMotivoCancelacion(reason);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointmentRepository.save(appointment);
    }
    
    /**
     * Obtiene las citas disponibles para un médico en un rango de fechas
     */
    public List<AppointmentDTO> getAvailableAppointmentsByDoctorAndDateRange(
            Long doctorId, LocalDate startDate, LocalDate endDate) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
                
        return appointmentRepository.findByMedicoAndEstadoAndFechaBetween(
                doctor, Appointment.Status.DISPONIBLE, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las citas disponibles para un médico en una fecha específica
     */
    public List<AppointmentDTO> getAvailableAppointmentsByDoctorAndDate(Long doctorId, String date) {
        LocalDate appointmentDate = LocalDate.parse(date, DATE_FORMAT);
        return getAvailableAppointmentsByDoctorAndDateRange(doctorId, appointmentDate, appointmentDate);
    }
    
    /**
     * Obtiene todas las citas disponibles para un médico
     */
    public List<AppointmentDTO> getAvailableAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
                
        return appointmentRepository.findByMedicoAndEstado(doctor, Appointment.Status.DISPONIBLE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las citas disponibles en el sistema
     */
    public List<AppointmentDTO> getAvailableAppointments() {
        // Obtener todas las citas disponibles
        List<Appointment> citasDisponibles = appointmentRepository.findByEstado(Appointment.Status.DISPONIBLE);
        
        // Convertir a DTO y ordenar por fecha y hora
        return citasDisponibles.stream()
                .map(this::convertToDTO)
                .sorted(Comparator.comparing(AppointmentDTO::getDate)
                                .thenComparing(AppointmentDTO::getHoraInicio))
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva cita
     */
    @Transactional
    public AppointmentDTO createAppointment(AppointmentRequest request) {
        // 1. Verificar que el médico existe
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado"));
        
        // 2. Verificar que el paciente existe
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado"));
        
        // 3. Verificar que el horario existe y está disponible
        Horario horario = horarioRepository.findById(request.getHorarioId())
                .orElseThrow(() -> new HorarioNotFoundException("Horario no encontrado"));
        
        // 4. Verificar que el horario pertenece al médico
        if (!horario.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("El horario no pertenece al médico especificado");
        }
        
        // 5. Verificar que la cita está disponible
        LocalDate fechaCita = request.getFecha();
        LocalTime horaInicio = request.getHoraInicio();
        LocalTime horaFin = horaInicio.plusMinutes(horario.getDuracionCita());
        
        if (!appointmentRepository.isHorarioDisponible(doctor.getId(), fechaCita, horaInicio, horaFin)) {
            throw new AppointmentNotAvailableException("El horario seleccionado ya no está disponible");
        }
        
        // 6. Crear la cita
        Appointment cita = Appointment.builder()
                .medico(doctor)
                .paciente(patient)
                .horario(horario)
                .fecha(fechaCita)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .estado(Appointment.Status.CONFIRMADA)
                .motivoConsulta(request.getMotivoConsulta())
                .notas(request.getNotas())
                .build();
        
        // 7. Guardar la cita
        Appointment savedCita = appointmentRepository.save(cita);
        
        return convertToDTO(savedCita);
    }

    /**
     * Actualiza el estado de una cita (implementación secundaria)
     */
    @Transactional
    public AppointmentDTO updateEstado(Long appointmentId, Appointment.Status newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        
        appointment.setEstado(newStatus);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        return convertToDTO(updatedAppointment);
    }
    
    /**
     * Cancela una cita
     */
    @Transactional
    public AppointmentDTO cancelAppointment(Long appointmentId, String motivoCancelacion) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        
        if (appointment.getEstado() != Appointment.Status.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden cancelar citas confirmadas");
        }
        
        appointment.setEstado(Appointment.Status.CANCELADA);
        appointment.setMotivoCancelacion(motivoCancelacion);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        
        return convertToDTO(updatedAppointment);
    }
    
    /**
     * Obtiene los horarios disponibles de un médico para una fecha específica
     */
    public List<HorarioDTO> getAvailableSlots(Long doctorId, LocalDate fecha) {
        // Obtener el día de la semana (LUNES, MARTES, etc.)
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        
        // Obtener el horario del médico para ese día de la semana
        List<Horario> horarios = horarioRepository.findByDoctorIdAndDiaSemanaAndActivoTrue(doctorId, diaSemana);
        
        // Obtener las citas existentes para ese médico en esa fecha
        List<Appointment> citasExistentes = appointmentRepository.findByMedicoIdAndFecha(doctorId, fecha);
        
        // Convertir a DTOs y marcar los horarios ocupados
        return horarios.stream()
                .map(horario -> {
                    HorarioDTO dto = convertToHorarioDTO(horario);
                    
                    // Verificar si hay una cita en este horario
                    boolean ocupado = citasExistentes.stream()
                            .anyMatch(cita -> cita.getHoraInicio().equals(horario.getHoraInicio()));
                    
                    dto.setDisponible(!ocupado);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte una entidad Appointment a su correspondiente DTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .doctorId(appointment.getMedico() != null ? appointment.getMedico().getId() : null)
                .patientId(appointment.getPaciente() != null ? appointment.getPaciente().getId() : null)
                .horarioId(appointment.getHorario() != null ? appointment.getHorario().getId() : null)
                .date(appointment.getFecha())
                .time(appointment.getHoraInicio())
                .status(appointment.getEstado() != null ? appointment.getEstado().name() : null)
                .motivoConsulta(appointment.getMotivoConsulta())
                .notes(appointment.getNotas())
                // Skip motivoCancelacion for now to avoid compilation errors
                .fechaCreacion(appointment.getFechaCreacion() != null ? appointment.getFechaCreacion().toString() : null)
                .fechaActualizacion(appointment.getFechaActualizacion() != null ? appointment.getFechaActualizacion().toString() : null)
                .build();
    }
    /**
     * Convierte una entidad Horario a DTO
     */
    private HorarioDTO convertToHorarioDTO(Horario horario) {
        return HorarioDTO.builder()
                .id(horario.getId())
                .doctorId(horario.getDoctor().getId())
                .diaSemana(horario.getDiaSemana())
                .horaInicio(horario.getHoraInicio())
                .horaFin(horario.getHoraFin())
                .duracionCita(horario.getDuracionCita())
                .intervaloEntreCitas(horario.getIntervaloEntreCitas())
                .activo(horario.isActivo())
                .build();
    }
    
    /**
     * Obtiene las citas de un médico por su nombre de usuario
     */
    public List<AppointmentDTO> getAppointmentsByDoctor(String username) {
        Doctor doctor = doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado"));
        
        List<Appointment> appointments = appointmentRepository.findByMedico(doctor);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Genera citas automáticamente para todos los doctores basado en sus horarios
     * @return Número de citas generadas
     */
    @Transactional
    public int generateAppointments() {
        int totalAppointments = 0;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(WEEKS_TO_GENERATE);
        
        // Obtener todos los doctores activos
        List<Doctor> doctors = doctorRepository.findAll().stream()
                .filter(d -> d.isActivo() && "APPROVED".equals(d.getStatus()))
                .collect(Collectors.toList());
        
        for (Doctor doctor : doctors) {
            // Obtener horarios del doctor activos
            List<Horario> horarios = horarioRepository.findByDoctorIdAndActivoTrue(doctor.getId());
            
            for (Horario horario : horarios) {
                LocalDate currentDate = startDate;
                
                // Generar citas para cada día en el rango
                while (!currentDate.isAfter(endDate)) {
                    // Verificar si el día de la semana coincide con el horario
                    if (currentDate.getDayOfWeek().name().equals(horario.getDiaSemana())) {
                        totalAppointments += generateDailyAppointments(doctor, horario, currentDate);
                    }
                    currentDate = currentDate.plusDays(1);
                }
            }
        }
        
        return totalAppointments;
    }
    
    /**
     * Genera las citas para un día específico según el horario del doctor
     */
    private int generateDailyAppointments(Doctor doctor, Horario horario, LocalDate date) {
        int appointmentsCreated = 0;
        LocalTime startTime = horario.getHoraInicio();
        LocalTime endTime = horario.getHoraFin();
        int duration = doctor.getAppointmentDuration() != null ? doctor.getAppointmentDuration() : 30; // minutos
        
        // Asegurarse de que la hora de inicio sea antes que la de fin
        if (startTime.isAfter(endTime)) {
            return 0;
        }
        
        LocalTime currentTime = startTime;
        
        // Generar citas en intervalos de 'duration' minutos
        while (currentTime.plusMinutes(duration).isBefore(endTime) || 
               currentTime.plusMinutes(duration).equals(endTime)) {
            
            // Verificar si ya existe una cita en este horario
            if (!appointmentRepository.existsByMedicoAndFechaAndHoraInicio(doctor, date, currentTime)) {
                Appointment appointment = new Appointment();
                appointment.setMedico(doctor);
                appointment.setFecha(date);
                appointment.setHoraInicio(currentTime);
                appointment.setHoraFin(currentTime.plusMinutes(duration));
                appointment.setStatus(Appointment.Status.DISPONIBLE);
                appointment.setHorario(horario);
                appointment.setFechaCreacion(LocalDateTime.now());
                appointment.setFechaActualizacion(LocalDateTime.now());
                
                try {
                    appointment = appointmentRepository.save(appointment);
                    log.info("Cita generada exitosamente para el médico {} el {} a las {}", 
                            doctor.getUser().getUsername(), date, currentTime);
                } catch (Exception e) {
                    log.error("Error al guardar la cita para el médico {} el {} a las {}: {}", 
                            doctor.getUser().getUsername(), date, currentTime, e.getMessage(), e);
                    throw e;
                }
                appointmentsCreated++;
            }
            
            currentTime = currentTime.plusMinutes(duration);
        }
        
        return appointmentsCreated;
    }
    
    /**
     * Genera citas para un médico específico basado en sus horarios
     * @param doctorId ID del médico
     * @return Número de citas generadas
     */
    @Transactional
    public int generateAppointmentsForDoctor(Long doctorId) {
        log.info("Generando citas para el médico con ID: {}", doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + doctorId));
        
        // Obtener horarios activos del médico
        List<Horario> horarios = horarioRepository.findByDoctorIdAndActivoTrue(doctorId);
        
        if (horarios.isEmpty()) {
            log.warn("No se encontraron horarios activos para el médico con ID: {}", doctorId);
            return 0;
        }
        
        int totalCitasGeneradas = 0;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(4); // Generar citas para las próximas 4 semanas
        
        for (Horario horario : horarios) {
            LocalDate currentDate = startDate;
            
            // Generar citas para cada día en el rango
            while (!currentDate.isAfter(endDate)) {
                // Verificar si el día de la semana coincide con el horario
                if (currentDate.getDayOfWeek() == horario.getDiaSemana()) {
                    totalCitasGeneradas += generateDailyAppointments(doctor, horario, currentDate);
                }
                currentDate = currentDate.plusDays(1);
            }
        }
        
        log.info("Total de citas generadas para el médico {}: {}", doctor.getUser().getUsername(), totalCitasGeneradas);
        return totalCitasGeneradas;
    }
    
    /**
     * Obtiene las citas de un paciente por su nombre de usuario
     */
    public List<AppointmentDTO> getAppointmentsByPatient(String username) {
        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        
        List<Appointment> appointments = appointmentRepository.findByPaciente(patient);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
