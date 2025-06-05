package com.example.proyectocitas.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.format.annotation.DateTimeFormat;

import com.example.proyectocitas.dto.AppointmentDTO;
import com.example.proyectocitas.dto.AppointmentRequest;
import com.example.proyectocitas.dto.BookingRequest;
import com.example.proyectocitas.dto.ScheduleRequest;
import com.example.proyectocitas.models.Appointment.Status;
import com.example.proyectocitas.services.AppointmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(request));
    }

    @PostMapping("/schedule/{doctorId}")
    public ResponseEntity<Void> generateSchedule(
            @PathVariable Long doctorId,
            @RequestBody ScheduleRequest scheduleRequest,
            @RequestParam(defaultValue = "4") int weeksInAdvance) {
        appointmentService.generateInitialAppointmentsForDoctor(doctorId, weeksInAdvance);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{appointmentId}/book")
    public ResponseEntity<AppointmentDTO> bookAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(appointmentService.scheduleAppointment(userDetails.getUsername(), appointmentId));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, Status.COMPLETED));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        appointmentService.cancelAppointment(id, userDetails.getUsername(), reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) Status status) {
        if (status != null) {
            return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorAndStatus(doctorId, status));
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(
            @PathVariable Long patientId,
            @RequestParam(required = false) Status status) {
        if (status != null) {
            return ResponseEntity.ok(appointmentService.getAppointmentsByPatientAndStatus(patientId, status));
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        System.out.println("Solicitud de citas disponibles recibida - doctorId: " + doctorId
                + ", date: " + date + ", startDate: " + startDate + ", endDate: " + endDate);

        try {
            Object result;
            if (doctorId != null && startDate != null && endDate != null) {
                System.out.println("Buscando citas por rango de fechas");
                result = appointmentService.getAvailableAppointmentsByDoctorAndDateRange(doctorId, startDate, endDate);
                System.out.println("Citas encontradas: " + (result != null ? ((Map<?, ?>) result).size() : 0));
            } else if (doctorId != null && date != null) {
                System.out.println("Buscando citas por fecha específica");
                result = appointmentService.getAvailableAppointmentsByDoctorAndDate(doctorId, date);
                System.out.println("Citas encontradas: " + (result != null ? ((List<?>) result).size() : 0));
            } else if (doctorId != null) {
                System.out.println("Buscando todas las citas del doctor");
                result = appointmentService.getAvailableAppointmentsByDoctor(doctorId);
                System.out.println("Citas encontradas: " + (result != null ? ((List<?>) result).size() : 0));
            } else {
                System.out.println("Buscando todas las citas disponibles");
                result = appointmentService.getAvailableAppointments();
                System.out.println("Citas encontradas: " + (result != null ? ((List<?>) result).size() : 0));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener citas disponibles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener citas disponibles");
        }
    }

    /**
     * Crea una cita de manera dinámica verificando disponibilidad en tiempo
     * real
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAppointmentDynamically(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody BookingRequest request) {
        try {
            System.out.println("=== DEBUG CREATE APPOINTMENT ===");
            System.out.println("Username: " + userDetails.getUsername());
            System.out.println("Request: " + request);
            System.out.println("Doctor ID: " + request.getDoctorId());
            System.out.println("Date: " + request.getDate());
            System.out.println("Time: " + request.getTime());
            System.out.println("Notes: " + request.getNotes());

            AppointmentDTO appointment = appointmentService.createAppointmentDynamically(
                    userDetails.getUsername(),
                    request.getDoctorId(),
                    request.getDateAsLocalDate(),
                    request.getTimeAsLocalTime(),
                    request.getNotes()
            );

            System.out.println("Cita creada exitosamente: " + appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
        } catch (Exception e) {
            System.err.println("Error al crear cita: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
