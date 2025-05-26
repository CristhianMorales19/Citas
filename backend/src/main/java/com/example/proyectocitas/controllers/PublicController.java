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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.models.Appointment;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Horario;
import com.example.proyectocitas.models.Schedule;
import com.example.proyectocitas.repositories.AppointmentRepository;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.services.DoctorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {
    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
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
        
        for (int i = 0; i < 3; i++) {
            LocalDate date = startDate.plusDays(i);
            String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3).toLowerCase();
            
            // Buscar el horario del médico para este día
            List<Horario> horarios = doctor.getWeeklySchedule().stream()
                    .filter(s -> s.getDay().equalsIgnoreCase(dayOfWeek))
                    .collect(Collectors.toList());
            
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
                        .anyMatch(appt -> appt.getTime().equals(slotTime));
                    
                    // Verificar si el slot está ocupado por una cita programada
                    boolean isBooked = existingAppointments.stream()
                        .filter(appt -> appt.getStatus() != Appointment.Status.DISPONIBLE)
                        .anyMatch(appt -> appt.getTime().equals(slotTime));
                    
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
}
