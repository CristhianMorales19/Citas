package com.example.proyectocitas.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.dto.ScheduleDTO;
import com.example.proyectocitas.dto.ScheduleRequest;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Horario;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.AppointmentRepository;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.UserRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public DoctorService(DoctorRepository doctorRepository, UserRepository userRepository, 
                        AppointmentRepository appointmentRepository, AppointmentService appointmentService) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
    }
    
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
    
    public DoctorDTO getDoctorByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de doctor no encontrado"));
        
        return convertToDTO(doctor);
    }
    
    @Transactional
    public DoctorDTO createOrUpdateDoctorProfile(String username, DoctorDTO doctorDTO) {
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
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
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
            
            // Añadir los nuevos horarios
            for (Map<String, Object> scheduleItem : scheduleList) {
                Horario horario = new Horario();
                String day = (String) scheduleItem.get("day");
                String startTime = (String) scheduleItem.get("startTime");
                String endTime = (String) scheduleItem.get("endTime");
                
                horario.setDiaSemana(DayOfWeek.valueOf(day));
                horario.setHoraInicio(LocalTime.parse(startTime));
                horario.setHoraFin(LocalTime.parse(endTime));
                horario.setDoctor(doctor);
                doctor.getHorarios().add(horario);
                
                // Generar citas disponibles para este horario
                try {
                    ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                            .diaSemana(DayOfWeek.valueOf(day))
                            .horaInicio(LocalTime.parse(startTime))
                            .horaFin(LocalTime.parse(endTime))
                            .duracionCita(doctor.getAppointmentDuration())
                            .build();
                    
                    // Generar citas para las próximas 4 semanas
                    appointmentService.generateAppointmentSlots(doctor.getId(), scheduleRequest, 4);
                } catch (Exception e) {
                    // Registrar el error pero no fallar la operación completa
                    System.err.println("Error generando citas para el día " + day + ": " + e.getMessage());
                }
            }
        }
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
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
