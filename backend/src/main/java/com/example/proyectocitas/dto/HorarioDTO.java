package com.example.proyectocitas.dto;

import com.example.proyectocitas.models.Doctor;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
public class HorarioDTO {
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
    
    // Constructor sin el campo disponible para compatibilidad
    public HorarioDTO(Long id, Long doctorId, DayOfWeek diaSemana, LocalTime horaInicio, 
                     LocalTime horaFin, Integer duracionCita, Integer intervaloEntreCitas, Boolean activo) {
        this.id = id;
        this.doctorId = doctorId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.duracionCita = duracionCita;
        this.intervaloEntreCitas = intervaloEntreCitas;
        this.activo = activo;
        this.disponible = true; // Por defecto está disponible
    }
    
    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
    
    // Builder method
    public static HorarioDTOBuilder builder() {
        return new HorarioDTOBuilder();
    }
    
    // Builder class
    public static class HorarioDTOBuilder {
        private HorarioDTO dto = new HorarioDTO();
        
        public HorarioDTOBuilder id(Long id) {
            dto.id = id;
            return this;
        }
        
        public HorarioDTOBuilder doctorId(Long doctorId) {
            dto.doctorId = doctorId;
            return this;
        }
        
        public HorarioDTOBuilder diaSemana(DayOfWeek diaSemana) {
            dto.diaSemana = diaSemana;
            return this;
        }
        
        public HorarioDTOBuilder horaInicio(LocalTime horaInicio) {
            dto.horaInicio = horaInicio;
            return this;
        }
        
        public HorarioDTOBuilder horaFin(LocalTime horaFin) {
            dto.horaFin = horaFin;
            return this;
        }
        
        public HorarioDTOBuilder duracionCita(Integer duracionCita) {
            dto.duracionCita = duracionCita;
            return this;
        }
        
        public HorarioDTOBuilder intervaloEntreCitas(Integer intervaloEntreCitas) {
            dto.intervaloEntreCitas = intervaloEntreCitas;
            return this;
        }
        
        public HorarioDTOBuilder activo(Boolean activo) {
            dto.activo = activo;
            return this;
        }
        
        public HorarioDTOBuilder disponible(Boolean disponible) {
            dto.disponible = disponible;
            return this;
        }
        
        public HorarioDTO build() {
            return dto;
        }
    }
}
