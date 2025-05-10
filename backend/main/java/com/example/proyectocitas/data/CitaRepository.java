package com.example.proyectocitas.data;

import com.example.proyectocitas.logic.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Integer> {
    @Query("SELECT c FROM Cita c WHERE "
            + "c.idPaciente.id = :idPaciente "
            + "AND (:nombreMedico IS NULL OR :nombreMedico = '' OR c.idMedico.nombre LIKE %:nombreMedico%) "
            + "AND (:estado IS NULL OR :estado = '' OR c.estado = :estado) "
            + "ORDER BY c.horarioIdHorario.dia DESC, c.horarioIdHorario.horaInicio DESC")
    List<Cita> buscarCitasPorPaciente(@Param("idPaciente") Integer idPaciente,
                                      @Param("nombreMedico") String nombreMedico,
                                      @Param("estado") String estado);


    @Query("SELECT c FROM Cita c WHERE "
            + "c.idMedico.id = :idMedico "
            + "AND (:nombrePaciente IS NULL OR :nombrePaciente = '' OR c.idPaciente.nombre LIKE %:nombrePaciente%) "
            + "AND (:estado IS NULL OR :estado = '' OR c.estado = :estado) "
            + "ORDER BY c.horarioIdHorario.dia DESC, c.horarioIdHorario.horaInicio DESC")
    List<Cita> buscarCitasPorMedico(@Param("idMedico") Integer idMedico,
                                      @Param("nombrePaciente") String nombrePaciente,
                                      @Param("estado") String estado);
}
