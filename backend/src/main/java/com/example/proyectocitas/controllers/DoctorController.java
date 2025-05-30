package com.example.proyectocitas.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.proyectocitas.dto.AppointmentDTO;
import com.example.proyectocitas.dto.DoctorDTO;
import com.example.proyectocitas.services.AppointmentService;
import com.example.proyectocitas.services.DoctorService;
import com.example.proyectocitas.services.FileStorageService;

@RestController
@RequestMapping("/api/medicos")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final FileStorageService fileStorageService;
    
    public DoctorController(DoctorService doctorService, AppointmentService appointmentService, FileStorageService fileStorageService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.fileStorageService = fileStorageService;
    }
    
    @GetMapping("/listaMedicos")
    public ResponseEntity<List<DoctorDTO>> searchDoctors(
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(doctorService.searchDoctors(specialty, location));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }
    
    @GetMapping("/{id}/schedule")
    public ResponseEntity<Map<String, Object>> getDoctorSchedule(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        DoctorDTO doctor = doctorService.getDoctorById(id);
        
        // Construir respuesta con formato esperado por el frontend
        Map<String, Object> response = new HashMap<>();
        response.put("id", doctor.getId());
        response.put("name", doctor.getName());
        response.put("specialty", doctor.getSpecialty());
        
        // Aquí se podría implementar una lógica más compleja para generar
        // slots de tiempo disponibles basados en el horario semanal del doctor
        // y las citas ya programadas
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getDoctorProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            DoctorDTO dto = doctorService.getDoctorByUsername(userDetails.getUsername());
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            // Si no existe perfil de doctor, devolver 404 y mensaje claro
            return ResponseEntity.status(404).body(Map.of(
                "error", "No existe perfil de doctor para este usuario"
            ));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<DoctorDTO> updateDoctorProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DoctorDTO doctorDTO) {
        return ResponseEntity.ok(doctorService.createOrUpdateDoctorProfile(
                userDetails.getUsername(), doctorDTO));
    }
    
    @GetMapping("/citas")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(userDetails.getUsername()));
    }
    
    /**
     * Endpoint para subir la foto de perfil del médico
     */    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Log para debugging
            System.out.println("=== UPLOAD PHOTO DEBUG ===");
            System.out.println("Username: " + userDetails.getUsername());
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            
            // Almacenar el archivo y obtener su nombre único
            String fileName = fileStorageService.storeFile(file);
            System.out.println("Stored file: " + fileName);
              // Construir la URL completa para acceder al archivo
            String fileDownloadUri = "/uploads/profile-photos/" + fileName;
            
            // Actualizar el perfil del médico con la URL de la imagen
            doctorService.updateDoctorPhotoUrl(userDetails.getUsername(), fileDownloadUri);
            System.out.println("Updated doctor photo URL successfully");
            
            // Responder con la URL de la imagen
            response.put("url", fileDownloadUri);
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException ex) {
            System.err.println("RuntimeException: " + ex.getMessage());
            response.put("error", ex.getMessage());
            response.put("type", "runtime_error");
            return ResponseEntity.status(404).body(response);
            
        } catch (Exception ex) {
            System.err.println("General Exception: " + ex.getMessage());
            ex.printStackTrace();
            response.put("error", "Error al subir el archivo: " + ex.getMessage());
            response.put("type", "general_error");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Endpoint para actualizar el horario semanal del médico
     */
    @PutMapping("/schedule")
    public ResponseEntity<DoctorDTO> updateDoctorSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> scheduleData) {
        
        return ResponseEntity.ok(doctorService.updateDoctorSchedule(
                userDetails.getUsername(), scheduleData));
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "API routing is working!");
        response.put("endpoint", "/api/medicos/test");
        return ResponseEntity.ok(response);
    }    @GetMapping("/test-generate-appointments/{doctorId}")
    public ResponseEntity<Map<String, Object>> testGenerateAppointments(@PathVariable Long doctorId) {
        System.out.println("=== ENDPOINT DE PRUEBA: Generando citas para doctor ID: " + doctorId + " ===");
        
        try {
            DoctorDTO doctor = doctorService.getDoctorById(doctorId);
            System.out.println("Doctor encontrado: " + doctor.getName());
            System.out.println("Duración de cita: " + doctor.getAppointmentDuration() + " minutos");
            System.out.println("Horarios configurados: " + (doctor.getWeeklySchedule() != null ? doctor.getWeeklySchedule().size() : 0));
            
            if (doctor.getWeeklySchedule() == null || doctor.getWeeklySchedule().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Doctor no tiene horarios configurados");
                return ResponseEntity.ok(response);
            }
              // Simular el mismo ScheduleRequest que se crea en updateDoctorSchedule
            java.time.LocalDate fechaInicio = java.time.LocalDate.now();
            java.time.LocalDate fechaFin = fechaInicio.plusWeeks(4);
            
            java.util.List<java.time.DayOfWeek> diasDisponibles = doctor.getWeeklySchedule().stream()
                    .map(schedule -> java.time.DayOfWeek.valueOf(schedule.getDay()))
                    .collect(java.util.stream.Collectors.toList());
            
            com.example.proyectocitas.dto.ScheduleRequest scheduleRequest = 
                com.example.proyectocitas.dto.ScheduleRequest.builder()
                    .diasDisponibles(diasDisponibles)
                    .horaInicio(java.time.LocalTime.parse(doctor.getWeeklySchedule().get(0).getStartTime()))
                    .horaFin(java.time.LocalTime.parse(doctor.getWeeklySchedule().get(0).getEndTime()))
                    .duracionCita(doctor.getAppointmentDuration())
                    .build();
            
            System.out.println("ScheduleRequest creado - Días disponibles: " + diasDisponibles.size());
            System.out.println("Fecha inicio: " + fechaInicio + ", Fecha fin: " + fechaFin);            // Generar citas iniciales para el médico (4 semanas por adelantado)
            appointmentService.generateInitialAppointmentsForDoctor(doctorId, 4);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Citas generadas exitosamente");
            response.put("doctorId", doctorId);
            response.put("doctorName", doctor.getName());
            
            System.out.println("=== FIN ENDPOINT DE PRUEBA ===");
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            System.err.println("ERROR en endpoint de prueba: " + ex.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + ex.getMessage());
            response.put("doctorId", doctorId);
            
            return ResponseEntity.ok(response);
        }
    }
}
