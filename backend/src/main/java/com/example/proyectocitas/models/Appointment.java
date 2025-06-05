package com.example.proyectocitas.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "cita")
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        DISPONIBLE, PENDIENTE, CONFIRMADA, EN_PROCESO, COMPLETADA, CANCELADA, NO_ASISTIO, AGENDADA;

        // Alias constants for backward compatibility
        public static final Status BOOKED = AGENDADA;
        public static final Status CONFIRMED = CONFIRMADA;
        public static final Status IN_PROGRESS = EN_PROCESO;
        public static final Status CANCELLED = CANCELADA;
        public static final Status COMPLETED = COMPLETADA;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente")
    private Patient paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico", nullable = false)
    private Doctor medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario")
    private Horario horario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, columnDefinition = "varchar(20) default 'DISPONIBLE'")
    private Status estado;

    @Column(name = "motivo_consulta")
    private String motivoConsulta;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    private String notas;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", insertable = false)
    private LocalDateTime fechaActualizacion;

    // Status getter/setter aliases for compatibility with existing code
    @Transient
    @JsonIgnore
    public Status getStatus() {
        return this.estado;
    }

    @Transient
    @JsonIgnore
    public void setStatus(Status status) {
        this.estado = status;
    }

    // Compatibility methods
    public void setDoctor(Doctor doctor) {
        this.medico = doctor;
    }

    public void setPatient(Patient patient) {
        this.paciente = patient;
    }

    public void setDate(LocalDate date) {
        this.fecha = date;
    }

    public void setDuration(int duration) {
        if (this.horaInicio != null) {
            this.horaFin = this.horaInicio.plusMinutes(duration);
        }
    }

    public void setUpdatedAt(LocalDateTime dateTime) {
        this.fechaActualizacion = dateTime;
    }

    public void setCancellationReason(String reason) {
        this.motivoCancelacion = reason;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = Status.DISPONIBLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Additional getters and setters
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Status getEstado() {
        return this.estado;
    }

    public void setEstado(Status estado) {
        this.estado = estado;
    }

    // Additional getter methods needed by the service
    public Long getId() {
        return this.id;
    }

    public Doctor getMedico() {
        return this.medico;
    }

    public Patient getPaciente() {
        return this.paciente;
    }

    public Horario getHorario() {
        return this.horario;
    }

    public LocalDate getFecha() {
        return this.fecha;
    }

    public LocalTime getHoraInicio() {
        return this.horaInicio;
    }

    public LocalTime getHoraFin() {
        return this.horaFin;
    }

    public String getMotivoConsulta() {
        return this.motivoConsulta;
    }

    public String getNotas() {
        return this.notas;
    }

    public LocalDateTime getFechaCreacion() {
        return this.fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return this.fechaActualizacion;
    }

    // Builder method
    public static AppointmentBuilder builder() {
        return new AppointmentBuilder();
    }

    // Builder class
    public static class AppointmentBuilder {

        private Appointment appointment = new Appointment();

        public AppointmentBuilder id(Long id) {
            appointment.id = id;
            return this;
        }

        public AppointmentBuilder paciente(Patient paciente) {

            appointment.paciente = paciente;

            return this;
        }

        public AppointmentBuilder medico(Doctor medico) {
            appointment.medico = medico;
            return this;
        }

        public AppointmentBuilder horario(Horario horario) {
            appointment.horario = horario;
            return this;
        }

        public AppointmentBuilder fecha(LocalDate fecha) {
            appointment.fecha = fecha;
            return this;
        }

        public AppointmentBuilder horaInicio(LocalTime horaInicio) {
            appointment.horaInicio = horaInicio;
            return this;
        }

        public AppointmentBuilder horaFin(LocalTime horaFin) {
            appointment.horaFin = horaFin;
            return this;
        }

        public AppointmentBuilder estado(Status estado) {
            appointment.estado = estado;
            return this;
        }

        public AppointmentBuilder motivoConsulta(String motivoConsulta) {
            appointment.motivoConsulta = motivoConsulta;
            return this;
        }

        public AppointmentBuilder motivoCancelacion(String motivoCancelacion) {
            appointment.motivoCancelacion = motivoCancelacion;
            return this;
        }

        public AppointmentBuilder notas(String notas) {
            appointment.notas = notas;
            return this;
        }

        public AppointmentBuilder fechaCreacion(LocalDateTime fechaCreacion) {
            appointment.fechaCreacion = fechaCreacion;
            return this;
        }

        public AppointmentBuilder fechaActualizacion(LocalDateTime fechaActualizacion) {
            appointment.fechaActualizacion = fechaActualizacion;
            return this;
        }

        public Appointment build() {
            return appointment;
        }
    }
}
