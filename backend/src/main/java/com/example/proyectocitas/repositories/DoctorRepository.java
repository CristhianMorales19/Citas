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
    
    List<Doctor> findByStatus(String status);
    
    Long countByStatus(String status);
    
    @Query("SELECT d FROM Doctor d WHERE " +
           "(:specialty is null or d.specialty = :specialty) AND " +
           "(:location is null or d.location = :location)")
    List<Doctor> findBySpecialtyAndLocation(
            @Param("specialty") String specialty, 
            @Param("location") String location);
}
