package com.example.proyectocitas.repositories;

import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    
    List<Horario> findByDoctorIdAndDiaSemanaAndActivoTrue(Long doctorId, DayOfWeek diaSemana);
    
    @Query("SELECT h FROM Horario h " +
           "WHERE h.doctor.id = :doctorId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.activo = true " +
           "AND h.horaInicio <= :horaFin " +
           "AND h.horaFin >= :horaInicio")
    List<Horario> findAvailableSlots(
            @Param("doctorId") Long doctorId,
            @Param("diaSemana") DayOfWeek diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );
    
    @Query("SELECT h FROM Horario h " +
           "WHERE h.doctor.id = :doctorId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.activo = true " +
           "AND h.horaInicio <= :hora " +
           "AND h.horaFin > :hora")
    Optional<Horario> findAvailableSlotAtTime(
            @Param("doctorId") Long doctorId,
            @Param("diaSemana") DayOfWeek diaSemana,
            @Param("hora") LocalTime hora
    );
    
    @Query("SELECT DISTINCT h.diaSemana FROM Horario h " +
           "WHERE h.doctor.id = :doctorId AND h.activo = true")
    List<DayOfWeek> findDiasDisponiblesByDoctorId(@Param("doctorId") Long doctorId);
    
    List<Horario> findByDoctorIdAndActivoTrue(Long doctorId);
    
    @Query("SELECT h FROM Horario h " +
           "JOIN h.doctor d " +
           "WHERE d.id = :doctorId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.activo = true " +
           "ORDER BY h.horaInicio")
    List<Horario> findHorariosDisponiblesByDoctorAndDia(
            @Param("doctorId") Long doctorId,
            @Param("diaSemana") DayOfWeek diaSemana
    );
}
