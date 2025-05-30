package com.example.proyectocitas.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "citas")
@EqualsAndHashCode(exclude = "citas")
@Entity
@Table(name = "horario")
public class Horario {    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Cambiado de "id_horario" a "id" para coincidir con la base de datos
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaSemana;
    
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;
    
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
    
    @Column(name = "duracion_cita", nullable = false)
    private Integer duracionCita; // en minutos
    
    @Column(name = "intervalo_entre_citas")
    private Integer intervaloEntreCitas; // en minutos
    
    private Boolean activo;
    
    @OneToMany(mappedBy = "horario")
    private List<Appointment> citas;
    
    // Methods for compatibility with Schedule class
    public Doctor getDoctor() {
        return this.doctor;
    }
    
    public String getDay() {
        return diaSemana != null ? diaSemana.toString() : null;
    }
    
    public String getStartTime() {
        return horaInicio != null ? horaInicio.toString() : null;
    }
    
    public String getEndTime() {
        return horaFin != null ? horaFin.toString() : null;
    }
    
    // Alias methods for compatibility
    public Boolean isActivo() {
        return activo;
    }
    
    // Alias for compatibility
    public Integer getDuracionCita() {
        return this.duracionCita;
    }
    
    // Additional setter methods
    public void setDiaSemana(DayOfWeek diaSemana) {
        this.diaSemana = diaSemana;
    }
    
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
    
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
    
    public void setDuracionCita(Integer duracionCita) {
        this.duracionCita = duracionCita;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public Long getId() {
        return this.id;
    }
    
    public DayOfWeek getDiaSemana() {
        return this.diaSemana;
    }
    
    public LocalTime getHoraInicio() {
        return this.horaInicio;
    }
    
    public LocalTime getHoraFin() {
        return this.horaFin;
    }
    
    public Integer getIntervaloEntreCitas() {
        return this.intervaloEntreCitas;
    }
}
