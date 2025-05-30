package com.example.proyectocitas.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.User;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
    
    // Find doctor by user's username
    @Query("SELECT d FROM Doctor d WHERE d.user.username = :username")
    Optional<Doctor> findByUserUsername(String username);
    
    List<Doctor> findByStatus(String status); // Ya existe
    
    // Si quieres buscar por 'approved', agrega:
    // List<Doctor> findByApprovedFalse();
    
    Long countByStatus(String status);
      @Query("SELECT d FROM Doctor d WHERE " +
           "(:specialty is null or d.especialidad = :specialty) AND " +
           "(:location is null or d.location = :location)")
    List<Doctor> findBySpecialtyAndLocation(
            @Param("specialty") String specialty, 
            @Param("location") String location);
}
