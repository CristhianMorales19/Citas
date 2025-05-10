package com.example.proyectocitas.data;

import com.example.proyectocitas.logic.Horario;
import com.example.proyectocitas.logic.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {
    List<Horario> findAllByIdMedico(Medico idMedico);

    List<Horario> findFirst3ByIdMedicoOrderByDiaAsc(Medico medico);
}
