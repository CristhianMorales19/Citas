package com.example.proyectocitas.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/medicos")
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
    public ResponseEntity<DoctorDTO> getDoctorProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(doctorService.getDoctorByUsername(userDetails.getUsername()));
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
     */
    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file) {
        
        // Almacenar el archivo y obtener su nombre único
        String fileName = fileStorageService.storeFile(file);
        
        // Construir la URL completa para acceder al archivo
        String fileDownloadUri = "/uploads/" + fileName;
        
        // Actualizar el perfil del médico con la URL de la imagen
        doctorService.updateDoctorPhotoUrl(userDetails.getUsername(), fileDownloadUri);
        
        // Responder con la URL de la imagen
        Map<String, String> response = new HashMap<>();
        response.put("url", fileDownloadUri);
        
        return ResponseEntity.ok(response);
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
}
