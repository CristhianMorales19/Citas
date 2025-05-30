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

    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
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
    private List<Horario> horarios = new ArrayList<>();

    @OneToMany(mappedBy = "medico")
    private List<Appointment> citas = new ArrayList<>();

    // Constructor vacío requerido por JPA
    public Doctor() {
        this.horarios = new ArrayList<>();
        this.citas = new ArrayList<>();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getCedulaProfesional() {
        return cedulaProfesional;
    }

    public void setCedulaProfesional(String cedulaProfesional) {
        this.cedulaProfesional = cedulaProfesional;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getCostoConsulta() {
        return costoConsulta;
    }

    public void setCostoConsulta(Double costoConsulta) {
        this.costoConsulta = costoConsulta;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean getProfileConfigured() {
        return profileConfigured;
    }

    public void setProfileConfigured(Boolean profileConfigured) {
        this.profileConfigured = profileConfigured;
    }

    public List<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<Horario> horarios) {
        this.horarios = horarios;
    }

    public List<Appointment> getCitas() {
        return citas;
    }

    public void setCitas(List<Appointment> citas) {
        this.citas = citas;
    }

    // Métodos de compatibilidad
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

    public Boolean isProfileConfigured() {
        return profileConfigured;
    }

    public String getPresentacion() {
        return getPresentation();
    }

    public void setPresentacion(String presentacion) {
        setPresentation(presentacion);
    }

    // Builder pattern estático
    public static DoctorBuilder builder() {
        return new DoctorBuilder();
    }

    public static class DoctorBuilder {
        private Doctor doctor;

        public DoctorBuilder() {
            this.doctor = new Doctor();
        }

        public DoctorBuilder id(Long id) {
            doctor.setId(id);
            return this;
        }

        public DoctorBuilder user(User user) {
            doctor.setUser(user);
            return this;
        }

        public DoctorBuilder especialidad(String especialidad) {
            doctor.setEspecialidad(especialidad);
            return this;
        }

        public DoctorBuilder cedulaProfesional(String cedulaProfesional) {
            doctor.setCedulaProfesional(cedulaProfesional);
            return this;
        }

        public DoctorBuilder descripcion(String descripcion) {
            doctor.setDescripcion(descripcion);
            return this;
        }

        public DoctorBuilder costoConsulta(Double costoConsulta) {
            doctor.setCostoConsulta(costoConsulta);
            return this;
        }

        public DoctorBuilder calificacion(Double calificacion) {
            doctor.setCalificacion(calificacion);
            return this;
        }

        public DoctorBuilder activo(Boolean activo) {
            doctor.setActivo(activo);
            return this;
        }

        public DoctorBuilder status(String status) {
            doctor.setStatus(status);
            return this;
        }

        public DoctorBuilder location(String location) {
            doctor.setLocation(location);
            return this;
        }

        public DoctorBuilder appointmentDuration(Integer appointmentDuration) {
            doctor.setAppointmentDuration(appointmentDuration);
            return this;
        }

        public DoctorBuilder presentation(String presentation) {
            doctor.setPresentation(presentation);
            return this;
        }

        public DoctorBuilder photoUrl(String photoUrl) {
            doctor.setPhotoUrl(photoUrl);
            return this;
        }

        public DoctorBuilder profileConfigured(Boolean profileConfigured) {
            doctor.setProfileConfigured(profileConfigured);
            return this;
        }

        public DoctorBuilder horarios(List<Horario> horarios) {
            doctor.setHorarios(horarios);
            return this;
        }

        public DoctorBuilder citas(List<Appointment> citas) {
            doctor.setCitas(citas);
            return this;
        }

        public Doctor build() {
            return doctor;
        }
    }
}
