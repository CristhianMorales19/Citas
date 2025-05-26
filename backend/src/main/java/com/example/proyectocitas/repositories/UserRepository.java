package com.example.proyectocitas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByUsername(String username);
    boolean existsByUsername(String username);
    Long countByRole(Role role);
}
