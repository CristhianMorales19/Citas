package com.example.proyectocitas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class ScheduleRequest {
    private Long id;
    private Long doctorId;
    private DayOfWeek diaSemana;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;
    
    private Integer duracionCita; // en minutos
    private Integer intervaloEntreCitas; // en minutos
    private Boolean activo;
    private Boolean disponible;
    
    // Para generación de horarios recurrentes
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<DayOfWeek> diasDisponibles;
    private Integer maxCitasPorDia;
    
    // Campos adicionales para flexibilidad
    private String descripcion;
    private String notas;
    
    // Constructor simplificado para compatibilidad
    public ScheduleRequest(LocalTime horaInicio, LocalTime horaFin, Integer duracionCita) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.duracionCita = duracionCita;
        this.intervaloEntreCitas = duracionCita; // Por defecto, el intervalo es igual a la duración
        this.activo = true;
        this.disponible = true;
    }
    
    // Métodos adicionales para compatibilidad con el código existente
    public LocalDate getStartDate() {
        return this.fechaInicio;
    }
    
    public LocalDate getEndDate() {
        return this.fechaFin;
    }
    
    public List<DayOfWeek> getDiasDisponibles() {
        return this.diasDisponibles;
    }
    
    // Additional methods for compatibility with existing code
    public String getDay() {
        return this.diaSemana != null ? this.diaSemana.toString() : null;
    }
    
    public String getStartTime() {
        return this.horaInicio != null ? this.horaInicio.toString() : null;
    }
    
    public String getEndTime() {
        return this.horaFin != null ? this.horaFin.toString() : null;
    }
    
    public Integer getDuracionCita() {
        return this.duracionCita;
    }
    
    // Direct getter methods for compatibility
    public LocalTime getHoraInicio() {
        return this.horaInicio;
    }
    
    public LocalTime getHoraFin() {
        return this.horaFin;
    }
    
    // Builder method
    public static ScheduleRequestBuilder builder() {
        return new ScheduleRequestBuilder();
    }
    
    // Builder class
    public static class ScheduleRequestBuilder {
        private ScheduleRequest request = new ScheduleRequest();
        
        public ScheduleRequestBuilder diaSemana(DayOfWeek diaSemana) {
            request.diaSemana = diaSemana;
            return this;
        }
        
        public ScheduleRequestBuilder horaInicio(LocalTime horaInicio) {
            request.horaInicio = horaInicio;
            return this;
        }
        
        public ScheduleRequestBuilder horaFin(LocalTime horaFin) {
            request.horaFin = horaFin;
            return this;
        }
        
        public ScheduleRequestBuilder duracionCita(Integer duracionCita) {
            request.duracionCita = duracionCita;
            return this;
        }
        
        public ScheduleRequestBuilder diasDisponibles(List<DayOfWeek> diasDisponibles) {
            request.diasDisponibles = diasDisponibles;
            return this;
        }
        
        public ScheduleRequest build() {
            return request;
        }
    }
}
