package com.example.proyectocitas.services;

import org.springframework.stereotype.Service;
import com.example.proyectocitas.dto.DoctorDTO;
import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorService {
    
    private final List<DoctorDTO> doctors = new ArrayList<>();
    
    public List<DoctorDTO> getAllDoctors() {
        return new ArrayList<>(doctors);
    }
    
    public DoctorDTO getDoctorById(Long id) {
        return doctors.stream()
                .filter(doctor -> doctor.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public List<DoctorDTO> searchDoctors(String name, String specialty) {
        return doctors.stream()
                .filter(doctor -> 
                    (name == null || doctor.getName().toLowerCase().contains(name.toLowerCase())) &&
                    (specialty == null || doctor.getSpecialty().toLowerCase().contains(specialty.toLowerCase()))
                )
                .toList();
    }
}
