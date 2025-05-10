package com.example.proyectocitas.data;

import com.example.proyectocitas.logic.Medico;
import com.example.proyectocitas.logic.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {
    Medico findByIdUsuario(Usuario idUsuario);

    @Query("SELECT m FROM Medico m WHERE "
            + "(:especialidad IS NULL OR :especialidad = '' OR m.especialidad = :especialidad) "
            + "AND (:ubicacion IS NULL OR :ubicacion = '' OR m.ubicacion = :ubicacion)")
    List<Medico> buscarPorFiltros(@Param("especialidad") String especialidad,
                                  @Param("ubicacion") String ubicacion);
}
