package com.example.proyectocitas.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

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

    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DoctorDTO> getApprovedDoctors() {
        return doctorRepository.findByStatus("APPROVED").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }      public List<DoctorDTO> getPendingDoctors() {
        System.out.println("=== DEBUG: Buscando médicos pendientes ===");
        
        List<Doctor> pendingDoctors = doctorRepository.findByStatus("PENDING");
        System.out.println("DEBUG: Médicos con status PENDING encontrados: " + pendingDoctors.size());
        
        for (Doctor doctor : pendingDoctors) {
            User user = doctor.getUser();
            System.out.println("DEBUG: Doctor encontrado - ID: " + doctor.getId() + 
                             ", Usuario: " + (user != null ? user.getName() : "NULL") + 
                             ", Status: " + doctor.getStatus() + 
                             ", Especialidad: " + doctor.getEspecialidad());
        }
        
        List<DoctorDTO> result = pendingDoctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        System.out.println("DEBUG: DTOs convertidos: " + result.size());
        System.out.println("=== DEBUG: Fin búsqueda médicos pendientes ===");
        
        return result;
    }
    
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));
        return convertToDTO(doctor);
    }
    
    public DoctorDTO getDoctorByUsername(String username) {
        // Buscar el doctor directamente por username para evitar problemas de referencia
        return doctorRepository.findByUserUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("No existe perfil de doctor para este usuario"));
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
        doctor.setPresentation(doctorDTO.getPresentation());
        doctor.setProfileConfigured(true);

        // --- NUEVA LÓGICA PARA HORARIOS ---
        if (doctorDTO.getWeeklySchedule() != null) {
            List<ScheduleDTO> nuevosHorarios = doctorDTO.getWeeklySchedule();
            List<Horario> horariosActuales = doctor.getHorarios() == null ? new ArrayList<>() : doctor.getHorarios();

            // Marcar como inactivos los horarios que ya no están en la nueva lista
            for (Horario horarioExistente : horariosActuales) {
                boolean sigueExistiendo = nuevosHorarios.stream().anyMatch(s ->
                        horarioExistente.getDiaSemana().name().equals(s.getDay()) &&
                        horarioExistente.getHoraInicio().toString().equals(s.getStartTime()) &&
                        horarioExistente.getHoraFin().toString().equals(s.getEndTime())
                );
                if (!sigueExistiendo) {
                    horarioExistente.setActivo(false);
                } else {
                    horarioExistente.setActivo(true);
                }
            }

            // Agregar los nuevos horarios que no existen aún
            for (ScheduleDTO s : nuevosHorarios) {
                boolean yaExiste = horariosActuales.stream().anyMatch(h ->
                        h.getDiaSemana().name().equals(s.getDay()) &&
                        h.getHoraInicio().toString().equals(s.getStartTime()) &&
                        h.getHoraFin().toString().equals(s.getEndTime())
                );
                if (!yaExiste) {
                    Horario horario = new Horario();
                    horario.setDoctor(doctor);
                    horario.setDiaSemana(DayOfWeek.valueOf(s.getDay()));
                    horario.setHoraInicio(LocalTime.parse(s.getStartTime()));
                    horario.setHoraFin(LocalTime.parse(s.getEndTime()));
                    horario.setDuracionCita(doctor.getAppointmentDuration());
                    horario.setActivo(true);
                    horariosActuales.add(horario);
                }
            }
            doctor.setHorarios(horariosActuales);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Generar citas automáticamente después de guardar los horarios
        if (doctorDTO.getWeeklySchedule() != null && !doctorDTO.getWeeklySchedule().isEmpty()) {
            try {
                appointmentService.generateInitialAppointmentsForDoctor(savedDoctor.getId(), 4);
            } catch (Exception e) {
                System.err.println("ERROR al generar citas automáticamente: " + e.getMessage());
                e.printStackTrace();
            }
        }

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
        // Log de depuración para verificar parámetros de entrada
        System.out.println("=== DoctorService.searchDoctors ===");
        System.out.println("Parámetros recibidos - Specialty: " + specialty + ", Location: " + location);
        
        // Obtener todos los doctores de la consulta
        List<Doctor> allDoctorsFromQuery = doctorRepository.findBySpecialtyAndLocation(specialty, location);
        System.out.println("Doctores encontrados en la consulta: " + allDoctorsFromQuery.size());
          // Log de cada doctor encontrado
        for (Doctor doctor : allDoctorsFromQuery) {
            User user = doctor.getUser();
            System.out.println("Doctor encontrado - ID: " + doctor.getId() + 
                             ", Nombre: " + (user != null ? user.getName() : "N/A") + 
                             ", Status: " + doctor.getStatus() + 
                             ", Especialidad: " + doctor.getEspecialidad());
        }
        
        // Filtrar por status APPROVED
        List<Doctor> approvedDoctors = allDoctorsFromQuery.stream()
                .filter(doctor -> "APPROVED".equals(doctor.getStatus()))
                .collect(Collectors.toList());
        
        System.out.println("Doctores con status APPROVED: " + approvedDoctors.size());
        
        // Convertir a DTO
        List<DoctorDTO> result = approvedDoctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        System.out.println("DTOs generados: " + result.size());
        System.out.println("=== Fin DoctorService.searchDoctors ===");
        
        return result;
    }
    
    public boolean isTimeSlotAvailable(Long doctorId, LocalDate date, LocalTime time) {
        return !appointmentRepository.existsByDoctorIdAndDateAndTime(doctorId, date, time);
    }
    
    public Long getTotalDoctorsCount() {
        return doctorRepository.countByStatus("APPROVED");
    }
    
    /**
     * Actualiza la URL de la foto de perfil del médico
     */    @Transactional
    public void updateDoctorPhotoUrl(String username, String photoUrl) {
        System.out.println("=== updateDoctorPhotoUrl DEBUG ===");
        System.out.println("Username: " + username);
        System.out.println("PhotoUrl: " + photoUrl);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        System.out.println("User found: " + user.getId() + " - " + user.getName());
        
        // Buscar doctor existente o crear uno nuevo si no existe
        Doctor doctor = doctorRepository.findByUser(user).orElse(null);
        
        if (doctor == null) {
            System.out.println("No doctor profile found, creating basic profile");
            // Crear un perfil de doctor básico si no existe
            doctor = Doctor.builder()
                    .user(user)
                    .status("PENDING")  // Estado inicial
                    .activo(true)
                    .profileConfigured(false)
                    .build();
        }
        
        System.out.println("Setting photo URL: " + photoUrl);
        doctor.setPhotoUrl(photoUrl);
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        System.out.println("Doctor saved successfully with ID: " + savedDoctor.getId());
        System.out.println("=== updateDoctorPhotoUrl END ===");
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
                horario.setDuracionCita(doctor.getAppointmentDuration()); // Set duration
                horario.setActivo(true); // Marcar el horario como activo
                doctor.getHorarios().add(horario);// Generar citas disponibles para este horario
                try {
                    System.out.println("=== INICIO GENERACIÓN DE CITAS ===");
                    System.out.println("Doctor ID: " + doctor.getId());
                    System.out.println("Día: " + day + ", Hora inicio: " + startTime + ", Hora fin: " + endTime);
                    System.out.println("Duración cita: " + doctor.getAppointmentDuration());
                    
                    // Generar citas para las próximas 4 semanas basándose en los horarios configurados
                    appointmentService.generateInitialAppointmentsForDoctor(doctor.getId(), 4);
                    
                    System.out.println("Generación de citas completada");
                    System.out.println("=== FIN GENERACIÓN DE CITAS ===");
                } catch (Exception e) {
                    // Registrar el error pero no fallar la operación completa
                    System.err.println("=== ERROR GENERANDO CITAS ===");
                    System.err.println("Error generando citas para el día " + day + ": " + e.getMessage());
                    e.printStackTrace();
                    System.err.println("=== FIN ERROR ===");
                }
            }
        }
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
    }    public DoctorDTO convertToDTO(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        
        User user = doctor.getUser();
        
        return DoctorDTO.builder()
                .id(doctor.getId())
                .userId(user != null ? user.getId() : null)
                .name((user != null && user.getName() != null && !user.getName().isEmpty()) ? user.getName() : "Sin nombre")
                .email(user != null ? user.getEmail() : null)
                .specialty(doctor.getEspecialidad())           // Maps especialidad -> specialty
                .consultationCost(doctor.getCostoConsulta())   // Maps costoConsulta -> consultationCost
                .location(doctor.getLocation())                .appointmentDuration(doctor.getAppointmentDuration() != null ? doctor.getAppointmentDuration() : 30)
                .presentation(doctor.getPresentation())        // Maps presentation -> presentation (fixed method call)
                .photoUrl(doctor.getPhotoUrl())               // Add missing photoUrl mapping
                .status(doctor.getStatus())
                .profileConfigured(doctor.getProfileConfigured() != null ? doctor.getProfileConfigured() : false)
                .weeklySchedule(convertScheduleToDTO(doctor.getHorarios())) // Add schedule conversion
                .build();
    }

    /**
     * Converts a list of Horario entities to ScheduleDTO list
     */
    private List<ScheduleDTO> convertScheduleToDTO(List<Horario> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            return new ArrayList<>();
        }
        return horarios.stream()
                .map(horario -> ScheduleDTO.builder()
                        .id(horario.getId())
                        .day(horario.getDiaSemana() != null ? horario.getDiaSemana().toString() : null)
                        .startTime(horario.getHoraInicio() != null ? horario.getHoraInicio().toString() : null)
                        .endTime(horario.getHoraFin() != null ? horario.getHoraFin().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }    // Método temporal de depuración para obtener todos los doctores
    public List<DoctorDTO> getAllDoctorsDebug() {
        System.out.println("=== DEBUG: getAllDoctorsDebug method called ===");
        List<Doctor> allDoctors = doctorRepository.findAll();
        System.out.println("DEBUG: Total doctors found: " + allDoctors.size());
        
        for (Doctor doctor : allDoctors) {
            User user = doctor.getUser();
            System.out.println("DEBUG: Doctor - ID: " + doctor.getId() + 
                             ", Usuario: " + (user != null ? user.getName() : "NULL") + 
                             ", Username: " + (user != null ? user.getUsername() : "NULL") +
                             ", Status: '" + doctor.getStatus() + "'" + 
                             ", Especialidad: " + doctor.getEspecialidad() +
                             ", Activo: " + doctor.getActivo());
        }
        
        return allDoctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Method to check all status values in database
    public Map<String, Long> getStatusCounts() {
        System.out.println("=== DEBUG: getStatusCounts method called ===");
        List<Doctor> allDoctors = doctorRepository.findAll();
        
        Map<String, Long> statusCounts = allDoctors.stream()
                .collect(Collectors.groupingBy(
                    doctor -> doctor.getStatus() != null ? doctor.getStatus() : "NULL",
                    Collectors.counting()
                ));
        
        System.out.println("DEBUG: Status counts:");
        statusCounts.forEach((status, count) -> 
            System.out.println("  Status: '" + status + "' -> Count: " + count));
        
        return statusCounts;
    }
}
