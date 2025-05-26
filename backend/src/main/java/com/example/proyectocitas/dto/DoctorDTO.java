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
    private Long userId;
    private String name;
    private String email;
    private String specialty;
    private String location;
    private double consultationCost;
    private int appointmentDuration;
    private String presentation;
    private String photoUrl;
    private String status; // PENDING, APPROVED, REJECTED
    private boolean profileConfigured;
    private List<ScheduleDTO> weeklySchedule;
    
    // Getter methods
    public Long getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getSpecialty() {
        return this.specialty;
    }
    
    public double getConsultationCost() {
        return this.consultationCost;
    }
    
    public String getLocation() {
        return this.location;
    }
    
    public int getAppointmentDuration() {
        return this.appointmentDuration;
    }
    
    public String getPresentation() {
        return this.presentation;
    }
    
    public List<ScheduleDTO> getWeeklySchedule() {
        return this.weeklySchedule;
    }
    
    // Adding builder method
    public static DoctorDTOBuilder builder() {
        return new DoctorDTOBuilder();
    }
    
    // Builder class
    public static class DoctorDTOBuilder {
        private DoctorDTO dto = new DoctorDTO();
        
        public DoctorDTOBuilder id(Long id) {
            dto.id = id;
            return this;
        }
        
        public DoctorDTOBuilder userId(Long userId) {
            dto.userId = userId;
            return this;
        }
        
        public DoctorDTOBuilder name(String name) {
            dto.name = name;
            return this;
        }
        
        public DoctorDTOBuilder email(String email) {
            dto.email = email;
            return this;
        }
        
        public DoctorDTOBuilder specialty(String specialty) {
            dto.specialty = specialty;
            return this;
        }
        
        public DoctorDTOBuilder consultationCost(double consultationCost) {
            dto.consultationCost = consultationCost;
            return this;
        }
        
        public DoctorDTOBuilder location(String location) {
            dto.location = location;
            return this;
        }
        
        public DoctorDTOBuilder appointmentDuration(int appointmentDuration) {
            dto.appointmentDuration = appointmentDuration;
            return this;
        }
        
        public DoctorDTOBuilder presentation(String presentation) {
            dto.presentation = presentation;
            return this;
        }
        
        public DoctorDTOBuilder photoUrl(String photoUrl) {
            dto.photoUrl = photoUrl;
            return this;
        }
        
        public DoctorDTOBuilder status(String status) {
            dto.status = status;
            return this;
        }
        
        public DoctorDTOBuilder profileConfigured(boolean profileConfigured) {
            dto.profileConfigured = profileConfigured;
            return this;
        }
        
        public DoctorDTOBuilder weeklySchedule(List<ScheduleDTO> weeklySchedule) {
            dto.weeklySchedule = weeklySchedule;
            return this;
        }
        
        public DoctorDTO build() {
            return dto;
        }
    }
}
