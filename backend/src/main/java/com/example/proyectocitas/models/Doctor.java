package com.example.proyectocitas.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String especialidad;
    
    @Column(name = "cedula_profesional", unique = true)
    private String cedulaProfesional;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "costo_consulta", nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private Double costoConsulta;
    
    @Column(columnDefinition = "DECIMAL(3,2) DEFAULT 0.00")
    private Double calificacion;
    
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "appointment_duration")
    private Integer appointmentDuration;
    
    @Column(name = "presentation")
    private String presentation;
    
    @Column(name = "photo_url")
    private String photoUrl;
    
    @Column(name = "profile_configured")
    private Boolean profileConfigured;
    
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Horario> horarios = new ArrayList<>();
    
    @OneToMany(mappedBy = "medico")
    @Builder.Default
    private List<Appointment> citas = new ArrayList<>();
    
    // Additional getters and setters for the new fields
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getAppointmentDuration() {
        return appointmentDuration;
    }

    public void setAppointmentDuration(Integer appointmentDuration) {
        this.appointmentDuration = appointmentDuration;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }
    
    // Compatibility methods
    public Long getId() {
        return this.id;
    }
    
    public String getSpecialty() {
        return this.especialidad;
    }
    
    public void setSpecialty(String specialty) {
        this.especialidad = specialty;
    }
    
    public Double getConsultationCost() {
        return this.costoConsulta;
    }
    
    public void setConsultationCost(double consultationCost) {
        this.costoConsulta = consultationCost;
    }
    
    public List<Horario> getWeeklySchedule() {
        return this.horarios;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean isProfileConfigured() {
        return profileConfigured;
    }

    public void setProfileConfigured(Boolean profileConfigured) {
        this.profileConfigured = profileConfigured;
    }
    
    // Additional getter methods
    public User getUser() {
        return this.user;
    }
    
    public String getEspecialidad() {
        return this.especialidad;
    }
    
    public Double getCostoConsulta() {
        return this.costoConsulta;
    }
    
    public String getPresentacion() {
        return this.presentation;
    }
    
    public Boolean getProfileConfigured() {
        return this.profileConfigured;
    }
    
    public List<Horario> getHorarios() {
        return this.horarios;
    }
    
    public void setHorarios(ArrayList<Object> horarios) {
        this.horarios.clear();
        if (horarios != null) {
            for (Object obj : horarios) {
                if (obj instanceof Horario) {
                    this.horarios.add((Horario) obj);
                }
            }
        }
    }
    
    // Additional setter methods
    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
    
    public void setCostoConsulta(double costoConsulta) {
        this.costoConsulta = costoConsulta;
    }
    
    public void setPresentacion(String presentacion) {
        this.presentation = presentacion;
    }
    
    // Removed duplicate setPhotoUrl, setLocation, setStatus, and setAppointmentDuration methods as they already exist in the class
    
    // Static builder method
    public static DoctorBuilder builder() {
        return new DoctorBuilder();
    }
    
    // Builder class
    public static class DoctorBuilder {
        private Doctor doctor = new Doctor();
        
        public DoctorBuilder user(User user) {
            doctor.user = user;
            return this;
        }
        
        public DoctorBuilder especialidad(String especialidad) {
            doctor.especialidad = especialidad;
            return this;
        }
        
        public DoctorBuilder costoConsulta(Double costoConsulta) {
            doctor.costoConsulta = costoConsulta;
            return this;
        }
        
        public DoctorBuilder presentation(String presentation) {
            doctor.presentation = presentation;
            return this;
        }
        
        public DoctorBuilder photoUrl(String photoUrl) {
            doctor.photoUrl = photoUrl;
            return this;
        }
        
        public DoctorBuilder location(String location) {
            doctor.location = location;
            return this;
        }
        
        public DoctorBuilder appointmentDuration(Integer appointmentDuration) {
            doctor.appointmentDuration = appointmentDuration;
            return this;
        }
        
        public DoctorBuilder status(String status) {
            doctor.status = status;
            return this;
        }
        
        public DoctorBuilder profileConfigured(Boolean profileConfigured) {
            doctor.profileConfigured = profileConfigured;
            return this;
        }
        
        public DoctorBuilder horarios(List<Horario> horarios) {
            doctor.horarios = horarios;
            return this;
        }
        
        public Doctor build() {
            return doctor;
        }
    }
}
