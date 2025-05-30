package com.example.proyectocitas.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.dto.ScheduleDTO;
import com.example.proyectocitas.dto.ScheduleRequest;
import com.example.proyectocitas.models.Appointment;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Horario;
import com.example.proyectocitas.models.Schedule;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.AppointmentRepository;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DoctorDTO> getApprovedDoctors() {
        return doctorRepository.findByStatus("APPROVED").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DoctorDTO> getPendingDoctors() {
        return doctorRepository.findByStatus("PENDING").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
        return convertToDTO(doctor);
    }
    
    @Transactional
    public DoctorDTO getDoctorByUsername(String username) {
        try {
            System.out.println("Buscando usuario: " + username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
            
            System.out.println("Usuario encontrado, ID: " + user.getId());
            
            // Try to find by user first
            Optional<Doctor> existingDoctor = doctorRepository.findByUser(user);
            if (existingDoctor.isPresent()) {
                System.out.println("Doctor encontrado para el usuario: " + existingDoctor.get().getId());
                return convertToDTO(existingDoctor.get());
            }
            
            // If not found, try by username
            existingDoctor = doctorRepository.findByUserUsername(username);
            if (existingDoctor.isPresent()) {
                System.out.println("Doctor encontrado por username: " + existingDoctor.get().getId());
                return convertToDTO(existingDoctor.get());
            }
            
            // If still not found, create a new doctor profile
            System.out.println("Creando nuevo perfil de doctor para: " + username);
            Doctor newDoctor = Doctor.builder()
                    .user(user)
                    .status("PENDING")
                    .profileConfigured(false)
                    .activo(true)
                    .costoConsulta(0.0)
                    .calificacion(0.0)
                    .especialidad("General")  // Default specialty
                    .appointmentDuration(30)  // Default appointment duration in minutes
                    .location("")  // Empty location by default
                    .presentation("")  // Empty presentation by default
                    .build();
                    
            Doctor savedDoctor = doctorRepository.save(newDoctor);
            System.out.println("Nuevo perfil de doctor creado con ID: " + savedDoctor.getId());
            return convertToDTO(savedDoctor);
            
        } catch (Exception e) {
            System.err.println("Error en getDoctorByUsername para usuario " + username + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener el perfil del doctor: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public DoctorDTO createOrUpdateDoctorProfile(String username, DoctorDTO doctorDTO) {
        System.out.println("Iniciando createOrUpdateDoctorProfile para el usuario: " + username);
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            Doctor doctor = doctorRepository.findByUser(user)
                    .orElse(Doctor.builder()
                            .user(user)
                            .status("PENDING")
                            .build());
            
            doctor.setEspecialidad(doctorDTO.getSpecialty());
            doctor.setCostoConsulta(doctorDTO.getConsultationCost());
            doctor.setLocation(doctorDTO.getLocation());
            doctor.setAppointmentDuration(doctorDTO.getAppointmentDuration());
            doctor.setPresentacion(doctorDTO.getPresentation());
            
            // Marcar que el perfil ha sido configurado
            doctor.setProfileConfigured(true);
            
            // Update schedules
            if (doctorDTO.getWeeklySchedule() != null) {
                if (doctor.getHorarios() == null) {
                    doctor.setHorarios(new ArrayList<>());
                } else {
                    doctor.getHorarios().clear();
                }
                
                for (ScheduleDTO scheduleDTO : doctorDTO.getWeeklySchedule()) {
                    Horario horario = new Horario();
                    horario.setDoctor(doctor);
                    horario.setDiaSemana(DayOfWeek.valueOf(scheduleDTO.getDay()));
                    horario.setHoraInicio(LocalTime.parse(scheduleDTO.getStartTime()));
                    horario.setHoraFin(LocalTime.parse(scheduleDTO.getEndTime()));
                    horario.setDuracionCita(doctor.getAppointmentDuration());
                    
                    doctor.getHorarios().add(horario);
                }
            }
            
            // Guardar el médico con sus horarios
            Doctor savedDoctor = doctorRepository.save(doctor);
            System.out.println("Médico guardado con ID: " + savedDoctor.getId());
            
            // Generar citas para los horarios guardados
            if (savedDoctor.getHorarios() != null && !savedDoctor.getHorarios().isEmpty()) {
                System.out.println("Generando citas para " + savedDoctor.getHorarios().size() + " horarios");
                
                for (Horario horario : savedDoctor.getHorarios()) {
                    try {
                        System.out.println("Procesando horario: " + horario.getDiaSemana() + " de " + 
                                         horario.getHoraInicio() + " a " + horario.getHoraFin());
                        
                        // Crear lista de días disponibles
                        List<DayOfWeek> diasDisponibles = new ArrayList<>();
                        diasDisponibles.add(horario.getDiaSemana());
                        
                        // Crear ScheduleRequest
                        ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                                .diaSemana(horario.getDiaSemana())
                                .horaInicio(horario.getHoraInicio())
                                .horaFin(horario.getHoraFin())
                                .duracionCita(horario.getDuracionCita())
                                .diasDisponibles(diasDisponibles)
                                .fechaInicio(LocalDate.now())
                                .fechaFin(LocalDate.now().plusWeeks(4))
                                .build();
                        
                        System.out.println("Generando citas para las próximas 4 semanas...");
                        appointmentService.generateAppointmentSlots(savedDoctor.getId(), scheduleRequest, 4);
                        System.out.println("Citas generadas exitosamente");
                        
                    } catch (Exception e) {
                        System.err.println("Error generando citas para el horario: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("No hay horarios para generar citas");
            }
            
            return convertToDTO(savedDoctor);
            
        } catch (Exception e) {
            System.err.println("Error en createOrUpdateDoctorProfile: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Transactional
    public DoctorDTO approveDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
        
        doctor.setStatus("APPROVED");
        doctor.setProfileConfigured(false); // Marcar que el perfil aún no ha sido configurado
        doctorRepository.save(doctor);
        
        return convertToDTO(doctor);
    }
    
    @Transactional
    public DoctorDTO rejectDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
        
        doctor.setStatus("REJECTED");
        doctorRepository.save(doctor);
        
        return convertToDTO(doctor);
    }
    
    public List<DoctorDTO> searchDoctors(String specialty, String location) {
        return doctorRepository.findBySpecialtyAndLocation(specialty, location).stream()
                .filter(doctor -> "APPROVED".equals(doctor.getStatus()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public boolean isTimeSlotAvailable(Long doctorId, LocalDate date, LocalTime time) {
        return !appointmentRepository.existsByDoctorIdAndDateAndTime(doctorId, date, time);
    }
    
    public Long getTotalDoctorsCount() {
        return doctorRepository.countByStatus("APPROVED");
    }
    
    /**
     * Actualiza la URL de la foto de perfil del médico
     */
    @Transactional
    public void updateDoctorPhotoUrl(String username, String photoUrl) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de doctor no encontrado"));
        
        doctor.setPhotoUrl(photoUrl);
        doctorRepository.save(doctor);
    }
    
    /**
     * Actualiza el horario semanal del médico desde un mapa de datos
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public DoctorDTO updateDoctorSchedule(String username, Map<String, Object> scheduleData) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de doctor no encontrado"));
        
        // Obtener el horario desde el mapa de datos
        List<Map<String, Object>> scheduleList = (List<Map<String, Object>>) scheduleData.get("schedule");
        
        if (scheduleList != null) {
            // Limpiar el horario actual
            doctor.getHorarios().clear();
            
            // Primero guardamos los horarios para que tengan un ID
            doctor = doctorRepository.save(doctor);
            
            // Añadir los nuevos horarios
            for (Map<String, Object> scheduleItem : scheduleList) {
                Horario horario = new Horario();
                String day = (String) scheduleItem.get("day");
                String startTime = (String) scheduleItem.get("startTime");
                String endTime = (String) scheduleItem.get("endTime");
                
                // Configurar el horario
                horario.setDiaSemana(DayOfWeek.valueOf(day));
                horario.setHoraInicio(LocalTime.parse(startTime));
                horario.setHoraFin(LocalTime.parse(endTime));
                horario.setDoctor(doctor);
                horario.setActivo(true);
                
                // Guardar el horario primero
                doctor.getHorarios().add(horario);
                doctor = doctorRepository.save(doctor);
                
                // Obtener el horario recién guardado para asegurarnos de que tiene un ID
                Horario horarioGuardado = doctor.getHorarios().stream()
                    .filter(h -> h.getDiaSemana() == DayOfWeek.valueOf(day) && 
                                h.getHoraInicio().equals(LocalTime.parse(startTime)) && 
                                h.getHoraFin().equals(LocalTime.parse(endTime)))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Error al guardar el horario"));
                
                // Generar citas disponibles para este horario
                try {
                    // Generar citas para este horario específico
                    int citasGeneradas = appointmentService.generateAppointmentsForDoctor(doctor.getId());
                    System.out.println("Se generaron " + citasGeneradas + " citas para el doctor " + doctor.getUser().getUsername());
                } catch (Exception e) {
                    // Registrar el error pero no fallar la operación completa
                    System.err.println("Error generando citas para el día " + day + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Doctor savedDoctor = doctorRepository.save(doctor);
            return convertToDTO(savedDoctor);
        }
        
        return convertToDTO(doctor);
    }
    
    public DoctorDTO convertToDTO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        
        return DoctorDTO.builder()
                .id(doctor.getId())
                .userId(doctor.getUser() != null ? doctor.getUser().getId() : null)
                .name(doctor.getUser() != null ? doctor.getUser().getName() : null)
                .email(doctor.getUser() != null ? doctor.getUser().getEmail() : null)
                .specialty(doctor.getEspecialidad())
                .consultationCost(doctor.getCostoConsulta())
                .location(doctor.getLocation())
                .appointmentDuration(doctor.getAppointmentDuration())
                .presentation(doctor.getPresentacion())
                .status(doctor.getStatus())
                .profileConfigured(doctor.getProfileConfigured() != null ? doctor.getProfileConfigured() : false)
                .build();
    }
}
