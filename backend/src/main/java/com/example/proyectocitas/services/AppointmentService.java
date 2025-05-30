package com.example.proyectocitas.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.proyectocitas.dto.AppointmentDTO;
import com.example.proyectocitas.dto.AppointmentRequest;
import com.example.proyectocitas.dto.HorarioDTO;
import com.example.proyectocitas.exceptions.AppointmentNotAvailableException;
import com.example.proyectocitas.exceptions.DoctorNotFoundException;
import com.example.proyectocitas.exceptions.HorarioNotFoundException;
import com.example.proyectocitas.exceptions.PatientNotFoundException;
import com.example.proyectocitas.exceptions.ResourceNotFoundException;
import com.example.proyectocitas.models.Appointment;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Horario;
import com.example.proyectocitas.models.Patient;
import com.example.proyectocitas.repositories.AppointmentRepository;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.HorarioRepository;
import com.example.proyectocitas.repositories.PatientRepository;

import jakarta.transaction.Transactional;

/**
 * Servicio para gestionar las citas médicas
 */
@Service
public class AppointmentService {    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final HorarioRepository horarioRepository;    public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
                             PatientRepository patientRepository, HorarioRepository horarioRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.horarioRepository = horarioRepository;
    }
      private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
                
        return appointmentRepository.findByDoctor(doctor).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas de un médico por estado
     */
    public List<AppointmentDTO> getAppointmentsByDoctorAndStatus(Long doctorId, Appointment.Status status) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
                
        return appointmentRepository.findByDoctorAndStatus(doctor, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas de un paciente
     */
    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con ID: " + patientId));
                
        return appointmentRepository.findByPatient(patient).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las citas de un paciente por estado
     */
    public List<AppointmentDTO> getAppointmentsByPatientAndStatus(Long patientId, Appointment.Status status) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Paciente no encontrado con ID: " + patientId));
                
        return appointmentRepository.findByPatientAndStatus(patient, status).stream()
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
    }    /**
     * Verifica si un horario está disponible para un médico en una fecha y hora específicas
     */
    public boolean isTimeSlotAvailable(Long doctorId, LocalDate date, LocalTime time) {
        return !appointmentRepository.existsByDoctorIdAndDateAndTime(doctorId, date, time);
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
                
        return appointmentRepository.findByDoctorAndStatusAndDateBetween(
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
                
        return appointmentRepository.findByDoctorAndStatus(doctor, Appointment.Status.DISPONIBLE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las citas disponibles en el sistema
     */
    public List<AppointmentDTO> getAvailableAppointments() {
        return appointmentRepository.findByEstado(Appointment.Status.DISPONIBLE)
                .stream()
                .map(this::convertToDTO)
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
        System.out.println("=== DEBUG AppointmentService.getAppointmentsByDoctor ===");
        System.out.println("Username recibido: " + username);
        
        Doctor doctor = doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado"));
        
        System.out.println("Doctor encontrado - ID: " + doctor.getId());
        
        List<Appointment> appointments = appointmentRepository.findByMedico(doctor);
        System.out.println("Citas encontradas en BD: " + appointments.size());
        
        for (Appointment appointment : appointments) {
            System.out.println("Cita encontrada - ID: " + appointment.getId() + 
                             ", Fecha: " + appointment.getFecha() + 
                             ", Hora: " + appointment.getHoraInicio() + 
                             ", Estado: " + appointment.getEstado());
        }
        
        List<AppointmentDTO> dtos = appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        System.out.println("DTOs generados: " + dtos.size());
        System.out.println("=== FIN DEBUG getAppointmentsByDoctor ===");
        
        return dtos;
    }
    
    /**
     * Obtiene las citas de un paciente por su nombre de usuario
     */
    public List<AppointmentDTO> getAppointmentsByPatient(String username) {
        Patient patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
        
        List<Appointment> appointments = appointmentRepository.findByPaciente(patient);        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Genera citas automáticamente para un médico cuando configura su horario por primera vez
     * Este método debe ser llamado desde DoctorService cuando el doctor guarda su perfil por primera vez
     */
    @Transactional
    public void generateInitialAppointmentsForDoctor(Long doctorId, int weeksInAdvance) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor no encontrado con ID: " + doctorId));
        
        // Obtener los horarios activos del médico
        List<Horario> horarios = horarioRepository.findByDoctorIdAndActivoTrue(doctorId);
        
        if (horarios.isEmpty()) {
            throw new IllegalStateException("El médico no tiene horarios configurados");
        }
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(weeksInAdvance);
        
        // Generar citas para cada día en el rango especificado
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            
            // Buscar horarios para este día de la semana
            List<Horario> horariosDelDia = horarios.stream()
                    .filter(h -> h.getDiaSemana() == dayOfWeek)
                    .collect(Collectors.toList());
            
            // Generar citas para cada horario de este día
            for (Horario horario : horariosDelDia) {
                generateAppointmentsForSpecificDay(doctor, currentDate, horario);
            }
            
            currentDate = currentDate.plusDays(1);
        }
    }
    
    /**
     * Genera citas disponibles para un día específico basándose en un horario
     */
    private void generateAppointmentsForSpecificDay(Doctor doctor, LocalDate date, Horario horario) {
        LocalTime startTime = horario.getHoraInicio();
        LocalTime endTime = horario.getHoraFin();
        int duration = horario.getDuracionCita();
        
        LocalTime currentTime = startTime;
        
        while (currentTime.plusMinutes(duration).isBefore(endTime) || 
               currentTime.plusMinutes(duration).equals(endTime)) {
            
            // Verificar si ya existe una cita en este horario
            if (!appointmentRepository.existsByMedicoAndFechaAndHoraInicio(doctor, date, currentTime)) {
                // Crear nueva cita disponible
                Appointment appointment = Appointment.builder()
                        .medico(doctor)
                        .fecha(date)
                        .horaInicio(currentTime)
                        .horaFin(currentTime.plusMinutes(duration))
                        .estado(Appointment.Status.DISPONIBLE)
                        .horario(horario)
                        .fechaCreacion(LocalDateTime.now())
                        .build();
                
                appointmentRepository.save(appointment);
            }
            
            // Mover al siguiente bloque de tiempo
            currentTime = currentTime.plusMinutes(duration);
        }
    }
}
