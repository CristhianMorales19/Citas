package com.example.proyectocitas.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDTO {
    private Long id;
    private String name;
    private String specialty;
    private String location;
    private double consultationCost;
    private int appointmentDuration;
    private String presentation;
    private String photoUrl;
    private String status; // PENDING, APPROVED, REJECTED
    private boolean profileConfigured;
    private List<ScheduleDTO> weeklySchedule;
}
