package com.example.proyectocitas.repositories;

import com.example.proyectocitas.models.Appointment;
import com.example.proyectocitas.models.Appointment.Status;
import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Métodos para buscar por médico
    List<Appointment> findByMedico(Doctor medico);
    List<Appointment> findByMedicoAndEstado(Doctor medico, Status estado);
    
    // Métodos para buscar por paciente
    List<Appointment> findByPaciente(Patient paciente);
    List<Appointment> findByPacienteAndEstado(Patient paciente, Status estado);
    
    // Métodos para buscar por ID
    List<Appointment> findByPacienteId(Long pacienteId);
    List<Appointment> findByMedicoId(Long medicoId);
    
    // Métodos para buscar por fecha
    List<Appointment> findByMedicoAndFecha(Doctor medico, LocalDate fecha);
    List<Appointment> findByMedicoAndEstadoAndFechaBetween(Doctor medico, Status estado, LocalDate startDate, LocalDate endDate);
    
    // Métodos de existencia
    boolean existsByMedicoIdAndFechaAndHoraInicio(Long medicoId, LocalDate fecha, LocalTime horaInicio);
    boolean existsByMedicoAndFechaAndHoraInicio(Doctor medico, LocalDate fecha, LocalTime horaInicio);
    
    // Métodos para buscar por estado
    List<Appointment> findByEstado(Status estado);
    
    // Consultas personalizadas
    @Query("SELECT a FROM Appointment a WHERE a.medico.id = :doctorId AND a.fecha = :date AND a.estado = 'DISPONIBLE' ORDER BY a.horaInicio")
    List<Appointment> findAvailableByMedicoIdAndFecha(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    // Métodos para compatibilidad con el controlador público
    @Query("SELECT a FROM Appointment a WHERE a.medico.id = :doctorId AND a.fecha = :date ORDER BY a.horaInicio")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.medico.id = :doctorId AND a.fecha = :date AND a.estado = 'DISPONIBLE' ORDER BY a.horaInicio")
    List<Appointment> findAvailableByDoctorIdAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    // Métodos adicionales para compatibilidad
    default List<Appointment> findByDoctor(Doctor doctor) {
        return findByMedico(doctor);
    }
    
    default List<Appointment> findByDoctorAndEstado(Doctor doctor, Status estado) {
        return findByMedicoAndEstado(doctor, estado);
    }
    
    default List<Appointment> findByDoctorAndStatus(Doctor doctor, Status status) {
        return findByMedicoAndEstado(doctor, status);
    }
    
    default List<Appointment> findByPatient(Patient patient) {
        return findByPaciente(patient);
    }
    
    default List<Appointment> findByPatientAndEstado(Patient patient, Status estado) {
        return findByPacienteAndEstado(patient, estado);
    }
    
    default List<Appointment> findByPatientAndStatus(Patient patient, Status status) {
        return findByPacienteAndEstado(patient, status);
    }
    
    default List<Appointment> findByDoctorAndStatusAndDateBetween(Doctor doctor, Status status, LocalDate startDate, LocalDate endDate) {
        return findByMedicoAndEstadoAndFechaBetween(doctor, status, startDate, endDate);
    }
    
    default List<Appointment> findByDoctorAndDate(Doctor doctor, LocalDate date) {
        return findByMedicoAndFecha(doctor, date);
    }
    
    default boolean existsByDoctorIdAndDateAndTime(Long doctorId, LocalDate date, LocalTime time) {
        return existsByMedicoIdAndFechaAndHoraInicio(doctorId, date, time);
    }
    
    default boolean existsByDoctorAndDateAndTime(Doctor doctor, LocalDate date, LocalTime time) {
        return existsByMedicoAndFechaAndHoraInicio(doctor, date, time);
    }
    
    default List<Appointment> findByDoctorAndEstadoAndDateBetween(Doctor doctor, Status estado, LocalDate startDate, LocalDate endDate) {
        return findByMedicoAndEstadoAndFechaBetween(doctor, estado, startDate, endDate);
    }
    
    // Removed duplicate method findByDoctorAndStatusAndDateBetween
    
    // Obtener citas por médico y fecha
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.medico.id = :medicoId " +
           "AND a.fecha = :fecha " +
           "ORDER BY a.horaInicio")
    List<Appointment> findByMedicoIdAndFecha(
            @Param("medicoId") Long medicoId, 
            @Param("fecha") LocalDate fecha);
    
    // Obtener citas por paciente y fecha
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.paciente.id = :pacienteId " +
           "AND a.fecha = :fecha " +
           "ORDER BY a.horaInicio")
    List<Appointment> findByPacienteIdAndFecha(
            @Param("pacienteId") Long pacienteId, 
            @Param("fecha") LocalDate fecha);
    
    // Buscar citas para historial médico del paciente
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.paciente.id = :pacienteId " +
           "AND (a.fecha < CURRENT_DATE OR (a.fecha = CURRENT_DATE AND a.horaFin < CURRENT_TIME)) " +
           "ORDER BY a.fecha DESC, a.horaInicio DESC")
    List<Appointment> findHistorialCitasByPacienteId(@Param("pacienteId") Long pacienteId);
    
    // Verificar disponibilidad de horario
    @Query("SELECT CASE WHEN COUNT(a) = 0 THEN true ELSE false END FROM Appointment a " +
           "WHERE a.medico.id = :medicoId " +
           "AND a.fecha = :fecha " +
           "AND ((a.horaInicio < :horaFin AND a.horaFin > :horaInicio) " +
           "OR (a.horaInicio = :horaInicio AND a.horaFin = :horaFin)) " +
           "AND a.estado NOT IN ('CANCELADA', 'NO_ASISTIO')")
    boolean isHorarioDisponible(
            @Param("medicoId") Long medicoId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);
    
    // Obtener citas por médico y estado
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.medico.id = :medicoId " +
           "AND a.estado = :estado " +
           "AND a.fecha >= CURRENT_DATE " +
           "ORDER BY a.fecha, a.horaInicio")
    List<Appointment> findByMedicoIdAndEstado(
            @Param("medicoId") Long medicoId,
            @Param("estado") Status estado);
    
    // Obtener la próxima cita de un paciente
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.paciente.id = :pacienteId " +
           "AND (a.fecha > CURRENT_DATE OR (a.fecha = CURRENT_DATE AND a.horaInicio > CURRENT_TIME)) " +
           "AND a.estado NOT IN ('CANCELADA', 'COMPLETADA', 'NO_ASISTIO') " +
           "ORDER BY a.fecha, a.horaInicio")
    List<Appointment> findProximaCitaByPacienteId(@Param("pacienteId") Long pacienteId);
    
    // Obtener la próxima cita de un paciente (versión con límite)
    @Query(value = "SELECT a.* FROM cita a " +
           "WHERE a.id_paciente = :pacienteId " +
           "AND (a.fecha > CURRENT_DATE OR (a.fecha = CURRENT_DATE AND a.hora_inicio > CURRENT_TIME)) " +
           "AND a.estado NOT IN ('CANCELADA', 'COMPLETADA', 'NO_ASISTIO') " +
           "ORDER BY a.fecha, a.hora_inicio " +
           "LIMIT 1", nativeQuery = true)
    Optional<Appointment> findSiguienteCitaByPacienteId(@Param("pacienteId") Long pacienteId);
    
    // Obtener citas que necesitan recordatorio (por ejemplo, 24 horas antes)
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.estado = 'CONFIRMADA' " +
           "AND a.fecha = :fechaRecordatorio " +
           "AND a.horaInicio BETWEEN :horaInicio AND :horaFin")
    List<Appointment> findCitasParaRecordatorio(
            @Param("fechaRecordatorio") LocalDate fechaRecordatorio,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);
    
    // Buscar citas futuras de un médico
    @Query("SELECT a FROM Appointment a WHERE a.medico = :medico AND a.fecha >= CURRENT_DATE")
    List<Appointment> findByMedicoAndFechaFutura(
            @Param("medico") Doctor medico);
    
    // Buscar citas futuras por ID de médico
    @Query("SELECT a FROM Appointment a WHERE a.medico.id = :medicoId AND a.fecha >= :fecha")
    List<Appointment> findByMedicoIdAndFechaAfter(
            @Param("medicoId") Long medicoId,
            @Param("fecha") LocalDate fecha);
            
    // Buscar citas disponibles por rango de fechas
    @Query("SELECT a FROM Appointment a WHERE a.medico.id = :medicoId " +
           "AND a.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "AND a.estado = 'DISPONIBLE' AND a.paciente IS NULL")
    List<Appointment> findDisponiblesByMedicoIdAndFechaBetween(
            @Param("medicoId") Long medicoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}
