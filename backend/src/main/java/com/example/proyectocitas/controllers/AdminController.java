package com.example.proyectocitas.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyectocitas.dto.AppointmentDTO;
import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.dto.ScheduleRequest;
import com.example.proyectocitas.services.AppointmentService;
import com.example.proyectocitas.services.DoctorService;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    
    public AdminController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }
    
    @GetMapping("/medicos/pendientes")
    public ResponseEntity<List<DoctorDTO>> getPendingDoctors() {
        List<DoctorDTO> pendingDoctors = doctorService.getPendingDoctors();
        System.out.println("DEBUG AdminController: Devolviendo " + pendingDoctors.size() + " médicos pendientes");
        return ResponseEntity.ok(pendingDoctors);
    }
    
    @PostMapping("/medicos/{id}/aprobar")
    public ResponseEntity<DoctorDTO> approveDoctor(@PathVariable Long id) {
        DoctorDTO approvedDoctor = doctorService.approveDoctor(id);
        System.out.println("DEBUG AdminController: Médico aprobado - ID: " + id);
        return ResponseEntity.ok(approvedDoctor);
    }
    
    @PostMapping("/medicos/{id}/rechazar")
    public ResponseEntity<DoctorDTO> rejectDoctor(@PathVariable Long id) {
        DoctorDTO rejectedDoctor = doctorService.rejectDoctor(id);
        System.out.println("DEBUG AdminController: Médico rechazado - ID: " + id);
        return ResponseEntity.ok(rejectedDoctor);
    }
    
    // ================= ENDPOINTS PARA GESTIÓN DE CITAS =================
    
    /**
     * Generar citas automáticamente para un médico basado en su horario
     */
    @PostMapping("/citas/generar/{doctorId}")
    public ResponseEntity<Map<String, Object>> generateAppointments(
            @PathVariable Long doctorId,
            @RequestBody ScheduleRequest scheduleRequest,
            @RequestParam(defaultValue = "4") int weeksInAdvance) {
          try {
            appointmentService.generateInitialAppointmentsForDoctor(doctorId, weeksInAdvance);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Citas generadas exitosamente para el médico " + doctorId,
                "doctorId", doctorId,
                "weeksGenerated", weeksInAdvance
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error al generar citas: " + e.getMessage(),
                "doctorId", doctorId
            ));
        }
    }
    
    /**
     * Obtener todas las citas del sistema (para administración)
     */
    @GetMapping("/citas")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Obtener citas de un médico específico
     */
    @GetMapping("/citas/medico/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        return ResponseEntity.ok(appointments);
    }
    
    /**
     * Obtener citas disponibles por médico y rango de fechas
     */
    @GetMapping("/citas/disponibles/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAvailableAppointments(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<AppointmentDTO> appointments = appointmentService
                .getAvailableAppointmentsByDoctorAndDateRange(doctorId, startDate, endDate);
        return ResponseEntity.ok(appointments);
    }
      /**
     * Obtener estadísticas de citas
     */
    @GetMapping("/citas/estadisticas")
    public ResponseEntity<Map<String, Object>> getAppointmentStats() {        try {
            List<AppointmentDTO> allAppointments = appointmentService.getAllAppointments();
              long disponibles = allAppointments.stream()
                    .filter(a -> "DISPONIBLE".equals(a.getStatus()))
                    .count();
            
            long agendadas = allAppointments.stream()
                    .filter(a -> "AGENDADA".equals(a.getStatus()) || "CONFIRMADA".equals(a.getStatus()))
                    .count();
            
            long completadas = allAppointments.stream()
                    .filter(a -> "COMPLETADA".equals(a.getStatus()))
                    .count();
            
            long canceladas = allAppointments.stream()
                    .filter(a -> "CANCELADA".equals(a.getStatus()))
                    .count();
            
            Map<String, Object> stats = Map.of(
                "total", allAppointments.size(),
                "disponibles", disponibles,
                "agendadas", agendadas,
                "completadas", completadas,
                "canceladas", canceladas
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener estadísticas: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Generar citas para todos los médicos aprobados que tengan horarios configurados
     */
    @PostMapping("/citas/generar-todas")
    public ResponseEntity<Map<String, Object>> generateAllAppointments(
            @RequestParam(defaultValue = "4") int weeksInAdvance) {
          try {
            List<DoctorDTO> approvedDoctors = doctorService.getApprovedDoctors();
            int doctorsProcessed = 0;
            StringBuilder messages = new StringBuilder();
              for (DoctorDTO doctor : approvedDoctors) {                try {
                    // Generar citas para este médico usando sus horarios existentes
                    appointmentService.generateInitialAppointmentsForDoctor(doctor.getId(), weeksInAdvance);
                    
                    doctorsProcessed++;
                    messages.append("✓ Médico ID ").append(doctor.getId())
                           .append(" (").append(doctor.getName()).append(")\n");
                          
                } catch (Exception e) {
                    messages.append("✗ Error con médico ID ").append(doctor.getId())
                           .append(": ").append(e.getMessage()).append("\n");
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Proceso completado",
                "doctorsProcessed", doctorsProcessed,
                "totalDoctors", approvedDoctors.size(),
                "weeksGenerated", weeksInAdvance,
                "details", messages.toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error general al generar citas: " + e.getMessage()
            ));
        }
    }
}
