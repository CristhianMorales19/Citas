package com.example.proyectocitas.services;

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
import com.example.proyectocitas.models.Appointment;
import com.example.proyectocitas.models.Doctor;
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
        
        doctor.setSpecialty(doctorDTO.getSpecialty());
        doctor.setConsultationCost(doctorDTO.getConsultationCost());
        doctor.setLocation(doctorDTO.getLocation());
        doctor.setAppointmentDuration(doctorDTO.getAppointmentDuration());
        doctor.setPresentation(doctorDTO.getPresentation());
        
        // Marcar que el perfil ha sido configurado
        doctor.setProfileConfigured(true);
        
        // Update schedules
        if (doctorDTO.getWeeklySchedule() != null) {
            doctor.getWeeklySchedule().clear();
            
            for (ScheduleDTO scheduleDTO : doctorDTO.getWeeklySchedule()) {
                Schedule schedule = new Schedule();
                schedule.setDay(scheduleDTO.getDay());
                schedule.setStartTime(scheduleDTO.getStartTime());
                schedule.setEndTime(scheduleDTO.getEndTime());
                schedule.setDoctor(doctor);
                doctor.getWeeklySchedule().add(schedule);
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
            doctor.getWeeklySchedule().clear();
            
            // Añadir los nuevos horarios
            for (Map<String, Object> scheduleItem : scheduleList) {
                Schedule schedule = new Schedule();
                schedule.setDay((String) scheduleItem.get("day"));
                schedule.setStartTime((String) scheduleItem.get("startTime"));
                schedule.setEndTime((String) scheduleItem.get("endTime"));
                schedule.setDoctor(doctor);
                doctor.getWeeklySchedule().add(schedule);
            }
        }
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
    }
    
    public DoctorDTO convertToDTO(Doctor doctor) {
        List<ScheduleDTO> schedules = doctor.getWeeklySchedule().stream()
                .map(schedule -> ScheduleDTO.builder()
                        .id(schedule.getId())
                        .day(schedule.getDay())
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .build())
                .collect(Collectors.toList());
        
        return DoctorDTO.builder()
                .id(doctor.getId())
                .name(doctor.getUser().getName())
                .specialty(doctor.getSpecialty())
                .location(doctor.getLocation())
                .consultationCost(doctor.getConsultationCost())
                .appointmentDuration(doctor.getAppointmentDuration())
                .presentation(doctor.getPresentation())
                .photoUrl(doctor.getPhotoUrl())
                .status(doctor.getStatus())
                .profileConfigured(doctor.isProfileConfigured())
                .weeklySchedule(schedules)
                .build();
    }
}
