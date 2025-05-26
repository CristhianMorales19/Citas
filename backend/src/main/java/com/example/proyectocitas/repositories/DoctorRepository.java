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
    
    List<Doctor> findByStatus(String status);
    
    Long countByStatus(String status);
    
    @Query("SELECT d FROM Doctor d WHERE " +
           "(:especialidad is null or d.especialidad = :especialidad) AND " +
           "(:location is null or d.location = :location)")
    List<Doctor> findBySpecialtyAndLocation(
            @Param("especialidad") String especialidad, 
            @Param("location") String location);
}
