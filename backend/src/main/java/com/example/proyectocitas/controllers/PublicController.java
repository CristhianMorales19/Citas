package com.example.proyectocitas.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.models.Appointment;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Horario;
import com.example.proyectocitas.repositories.AppointmentRepository;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.HorarioRepository;
import com.example.proyectocitas.services.DoctorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@RequiredArgsConstructor
public class PublicController {
    private static final Logger log = LoggerFactory.getLogger(PublicController.class);    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final HorarioRepository horarioRepository;
    private final DoctorService doctorService;
    
    /**
     * Endpoint público para buscar médicos por especialidad y ubicación
     */
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> searchDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String location) {
        
        log.info("Búsqueda pública de médicos - Especialidad: {}, Ubicación: {}", specialty, location);
        
        // Usar el servicio existente para buscar médicos
        List<DoctorDTO> doctors = doctorService.searchDoctors(specialty, location);
        
        return ResponseEntity.ok(doctors);
    }
      /**
     * Debug endpoint to check all doctors in database regardless of status
     */
    @GetMapping("/doctors/debug")
    public ResponseEntity<List<DoctorDTO>> getAllDoctorsDebug() {
        log.info("DEBUG: Obteniendo todos los médicos");
        
        try {
            List<DoctorDTO> allDoctors = doctorService.getAllDoctorsDebug();
            log.info("DEBUG: Encontrados {} médicos en total", allDoctors.size());
            return ResponseEntity.ok(allDoctors);
        } catch (Exception e) {
            log.error("DEBUG: Error al obtener todos los médicos", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    /**
     * Debug endpoint to check only approved doctors
     */
    @GetMapping("/doctors/approved")
    public ResponseEntity<List<DoctorDTO>> getApprovedDoctorsDebug() {
        log.info("DEBUG: Obteniendo médicos aprobados");
        
        try {
            List<DoctorDTO> approvedDoctors = doctorService.getApprovedDoctors();
            log.info("DEBUG: Encontrados {} médicos aprobados", approvedDoctors.size());
            return ResponseEntity.ok(approvedDoctors);
        } catch (Exception e) {
            log.error("DEBUG: Error al obtener médicos aprobados", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    /**
     * Endpoint de debugging para obtener médicos pendientes
     */
    @GetMapping("/doctors/pending")
    public ResponseEntity<List<DoctorDTO>> getPendingDoctorsDebug() {
        log.info("DEBUG: Obteniendo médicos pendientes");
        
        try {
            List<DoctorDTO> pendingDoctors = doctorService.getPendingDoctors();
            log.info("DEBUG: Encontrados {} médicos pendientes", pendingDoctors.size());
            return ResponseEntity.ok(pendingDoctors);
        } catch (Exception e) {
            log.error("DEBUG: Error al obtener médicos pendientes", e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    /**
     * Endpoint público para obtener la disponibilidad de un médico para los próximos días
     */
    @GetMapping("/doctors/{doctorId}/availability")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        
        // Buscar el médico
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        
        // Solo mostrar médicos aprobados
        if (!"APPROVED".equals(doctor.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        
        // Convertir a DTO
        DoctorDTO doctorDTO = doctorService.convertToDTO(doctor);
          // Calcular disponibilidad para los próximos 3 días
        List<Map<String, Object>> availableDays = new ArrayList<>();
        
        // Debug: verificar horarios del doctor
        List<Horario> allSchedules = horarioRepository.findByDoctorIdAndActivoTrue(doctor.getId());
        log.info("Doctor ID {}: Found {} active schedules", doctor.getId(), allSchedules.size());
        for (Horario h : allSchedules) {
            log.info("Schedule: Day={}, Start={}, End={}, Active={}", 
                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), h.getActivo());
        }
        
        for (int i = 0; i < 3; i++) {
            LocalDate date = startDate.plusDays(i);
            String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3).toLowerCase();
            log.info("Checking availability for date: {}, dayOfWeek: {}", date, dayOfWeek);
            
            // Buscar el horario del médico para este día
            List<Horario> horarios = allSchedules.stream()
                    .filter(s -> s.getDiaSemana().toString().substring(0, 3).toLowerCase().equals(dayOfWeek))
                    .collect(Collectors.toList());
            
            log.info("Found {} schedules for day {}", horarios.size(), dayOfWeek);
            
            if (!horarios.isEmpty()) {
                Horario horario = horarios.get(0);
                
                // Buscar citas existentes para este día
                List<Appointment> existingAppointments = appointmentRepository
                        .findByDoctorIdAndDate(doctor.getId(), date);
                        
                // Buscar citas disponibles para este día
                List<Appointment> availableAppointments = appointmentRepository
                        .findAvailableByDoctorIdAndDate(doctor.getId(), date);
                
                // Crear slots de tiempo disponibles
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.toString());
                dayData.put("dayOfWeek", dayOfWeek);
                
                List<Map<String, Object>> slots = new ArrayList<>();
                
                // Convertir horarios de inicio y fin a LocalTime
                LocalTime startTime = horario.getHoraInicio();
                LocalTime endTime = horario.getHoraFin();
                
                // Crear slots de duración de cita
                LocalTime currentSlot = startTime;
                while (currentSlot.plusMinutes(doctor.getAppointmentDuration()).isBefore(endTime) || 
                       currentSlot.plusMinutes(doctor.getAppointmentDuration()).equals(endTime)) {
                    
                    // Create a final copy of currentSlot for use in lambda
                    final LocalTime slotTime = currentSlot;
                      // Verificar si el slot está disponible (tiene una cita con estado 'AVAILABLE')
                    boolean isAvailable = availableAppointments.stream()
                        .anyMatch(appt -> appt.getHoraInicio().equals(slotTime));
                    
                    // Verificar si el slot está ocupado por una cita programada
                    boolean isBooked = existingAppointments.stream()
                        .filter(appt -> appt.getStatus() != Appointment.Status.DISPONIBLE)
                        .anyMatch(appt -> appt.getHoraInicio().equals(slotTime));
                    
                    // Solo mostrar el slot si está disponible y no está ocupado
                    boolean showSlot = isAvailable && !isBooked;
                    
                    Map<String, Object> slotData = new HashMap<>();
                    slotData.put("time", currentSlot.toString());
                    slotData.put("available", showSlot);
                    
                    slots.add(slotData);
                    
                    // Avanzar al siguiente slot
                    currentSlot = currentSlot.plusMinutes(doctor.getAppointmentDuration());
                }
                
                dayData.put("slots", slots);
                availableDays.add(dayData);
            }
        }
        
        // Construir la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("doctor", doctorDTO);
        response.put("availableDays", availableDays);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint de debug temporal sin autenticación para verificar médicos
     */
    @GetMapping("/debug/doctors-status")
    public ResponseEntity<Map<String, Object>> debugDoctorsStatus() {
        try {
            List<Doctor> allDoctors = doctorRepository.findAll();
            List<Doctor> pendingDoctors = doctorRepository.findByStatus("PENDING");
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("totalDoctors", allDoctors.size());
            debug.put("pendingDoctorsCount", pendingDoctors.size());
            
            // Detalles de todos los médicos
            debug.put("allDoctors", allDoctors.stream()
                .map(d -> Map.of(
                    "id", d.getId(),
                    "name", d.getUser() != null ? d.getUser().getName() : "Sin nombre",
                    "username", d.getUser() != null ? d.getUser().getUsername() : "Sin username",
                    "status", d.getStatus() != null ? d.getStatus() : "NULL",
                    "specialty", d.getEspecialidad() != null ? d.getEspecialidad() : "Sin especialidad",
                    "active", d.getActivo() != null ? d.getActivo() : false
                ))
                .collect(Collectors.toList())
            );
            
            // Test del servicio
            List<DoctorDTO> pendingDoctorDTOs = doctorService.getPendingDoctors();
            debug.put("pendingDoctorDTOsCount", pendingDoctorDTOs.size());
            debug.put("pendingDoctorDTOs", pendingDoctorDTOs.stream()
                .map(dto -> Map.of(
                    "id", dto.getId(),
                    "name", dto.getName() != null ? dto.getName() : "Sin nombre",
                    "specialty", dto.getSpecialty() != null ? dto.getSpecialty() : "Sin especialidad",
                    "status", dto.getStatus() != null ? dto.getStatus() : "NULL"
                ))
                .collect(Collectors.toList())
            );
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "stackTrace", e.getStackTrace()[0].toString()
            ));
        }
    }
    
    /**
     * Debug endpoint to check status distribution of all doctors
     */
    @GetMapping("/doctors/status-counts")
    public ResponseEntity<Map<String, Long>> getStatusCounts() {
        log.info("DEBUG: Obteniendo conteo de status de médicos");
        
        try {
            Map<String, Long> statusCounts = doctorService.getStatusCounts();
            log.info("DEBUG: Status counts: {}", statusCounts);
            return ResponseEntity.ok(statusCounts);
        } catch (Exception e) {
            log.error("DEBUG: Error al obtener conteo de status", e);
            return ResponseEntity.ok(new HashMap<>());
        }
    }
}
