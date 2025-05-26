package com.example.proyectocitas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequest {
    private Long id;
    private Long doctorId;
    private Long patientId;
    private Long horarioId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime time;
    
    private String status;
    private String motivoConsulta;
    private String notas;
    private String motivoCancelacion;
    
    // Campos para la generación de horarios
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duracionCita; // en minutos
    private Integer intervaloEntreCitas; // en minutos
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer[] diasSemana; // 1-7 (Lunes-Domingo)
    
    // Métodos adicionales para compatibilidad con el código existente
    public Long getDoctorId() {
        return this.doctorId;
    }
    
    public Long getPatientId() {
        return this.patientId;
    }
    
    public Long getHorarioId() {
        return this.horarioId;
    }
    
    public LocalDate getFecha() {
        return this.date;
    }
    
    public LocalTime getHoraInicio() {
        return this.time;
    }
    
    public String getMotivoConsulta() {
        return this.motivoConsulta;
    }
    
    public String getNotas() {
        return this.notas;
    }
}
