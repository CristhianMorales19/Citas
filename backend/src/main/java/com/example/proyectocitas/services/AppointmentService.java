package com.example.proyectocitas.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyectocitas.dto.AppointmentDTO;
import com.example.proyectocitas.dto.AppointmentRequest;
import com.example.proyectocitas.models.Appointment;
import com.example.proyectocitas.models.Appointment.Status;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Patient;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.AppointmentRepository;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.PatientRepository;
import com.example.proyectocitas.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final DoctorService doctorService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        return convertToDTO(appointment);
    }
    
    public List<AppointmentDTO> getAppointmentsByDoctor(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de doctor no encontrado"));
        
        return appointmentRepository.findByDoctor(doctor).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AppointmentDTO> getAppointmentsByPatient(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de paciente no encontrado"));
        
        return appointmentRepository.findByPatient(patient).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public AppointmentDTO createAppointment(String username, AppointmentRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        Patient patient = patientRepository.findByUser(user)
                .orElseGet(() -> {
                    Patient newPatient = Patient.builder()
                            .user(user)
                            .build();
                    return patientRepository.save(newPatient);
                });
        
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
        
        LocalDate date = LocalDate.parse(request.getDate(), DATE_FORMATTER);
        LocalTime time = LocalTime.parse(request.getTime(), TIME_FORMATTER);
        
        // Verificar disponibilidad
        if (!doctorService.isTimeSlotAvailable(doctor.getId(), date, time)) {
            throw new IllegalStateException("El horario ya está ocupado");
        }
        
        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .date(date)
                .time(time)
                .status(Status.SCHEDULED)
                .notes(request.getNotes())
                .build();
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return convertToDTO(savedAppointment);
    }
    
    @Transactional
    public AppointmentDTO updateAppointmentStatus(Long appointmentId, Status status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        
        appointment.setStatus(status);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        return convertToDTO(savedAppointment);
    }
    
    @Transactional
    public void cancelAppointment(Long appointmentId, String username) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar si el usuario es el paciente de la cita o el doctor
        Patient patient = patientRepository.findByUser(user).orElse(null);
        Doctor doctor = doctorRepository.findByUser(user).orElse(null);
        
        if ((patient != null && appointment.getPatient().getId().equals(patient.getId())) ||
            (doctor != null && appointment.getDoctor().getId().equals(doctor.getId()))) {
            appointment.setStatus(Status.CANCELLED);
            appointmentRepository.save(appointment);
        } else {
            throw new IllegalStateException("No tiene permisos para cancelar esta cita");
        }
    }
    
    private AppointmentDTO convertToDTO(Appointment appointment) {
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getName())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getUser().getName())
                .date(appointment.getDate().format(DATE_FORMATTER))
                .time(appointment.getTime().format(TIME_FORMATTER))
                .status(appointment.getStatus().name())
                .notes(appointment.getNotes())
                .build();
    }
}
