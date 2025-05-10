package com.example.proyectocitas.data;

import com.example.proyectocitas.logic.Paciente;
import com.example.proyectocitas.logic.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

    Paciente findByIdUsuario(Usuario idUsuario);
}
