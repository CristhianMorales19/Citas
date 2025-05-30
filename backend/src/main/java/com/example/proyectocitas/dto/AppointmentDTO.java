package com.example.proyectocitas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private String patientName;
    private LocalDate date;
    private LocalTime time;
    private String status;
    private String notes;
    private String motivoConsulta;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String fechaCreacion;
    private String fechaActualizacion;
    private Long horarioId;
    
    // Compatibility method for getStatus()
    public String getStatus() {
        return this.status;
    }
    
    // Builder method
    public static AppointmentDTOBuilder builder() {
        return new AppointmentDTOBuilder();
    }
    
    // Builder class
    public static class AppointmentDTOBuilder {
        private AppointmentDTO dto = new AppointmentDTO();
        
        public AppointmentDTOBuilder id(Long id) {
            dto.id = id;
            return this;
        }
        
        public AppointmentDTOBuilder doctorId(Long doctorId) {
            dto.doctorId = doctorId;
            return this;
        }
        
        public AppointmentDTOBuilder doctorName(String doctorName) {
            dto.doctorName = doctorName;
            return this;
        }
        
        public AppointmentDTOBuilder patientId(Long patientId) {
            dto.patientId = patientId;
            return this;
        }
        
        public AppointmentDTOBuilder patientName(String patientName) {
            dto.patientName = patientName;
            return this;
        }
        
        public AppointmentDTOBuilder date(LocalDate date) {
            dto.date = date;
            return this;
        }
        
        public AppointmentDTOBuilder time(LocalTime time) {
            dto.time = time;
            return this;
        }
        
        public AppointmentDTOBuilder status(String status) {
            dto.status = status;
            return this;
        }
        
        public AppointmentDTOBuilder notes(String notes) {
            dto.notes = notes;
            return this;
        }
        
        public AppointmentDTOBuilder motivoConsulta(String motivoConsulta) {
            dto.motivoConsulta = motivoConsulta;
            return this;
        }
        
        public AppointmentDTOBuilder horaInicio(LocalTime horaInicio) {
            dto.horaInicio = horaInicio;
            return this;
        }
        
        public AppointmentDTOBuilder horaFin(LocalTime horaFin) {
            dto.horaFin = horaFin;
            return this;
        }
        
        public AppointmentDTOBuilder fechaCreacion(String fechaCreacion) {
            dto.fechaCreacion = fechaCreacion;
            return this;
        }
        
        public AppointmentDTOBuilder fechaActualizacion(String fechaActualizacion) {
            dto.fechaActualizacion = fechaActualizacion;
            return this;
        }
        
        public AppointmentDTOBuilder horarioId(Long horarioId) {
            dto.horarioId = horarioId;
            return this;
        }
        
        public AppointmentDTO build() {
            return dto;
        }
    }
}
