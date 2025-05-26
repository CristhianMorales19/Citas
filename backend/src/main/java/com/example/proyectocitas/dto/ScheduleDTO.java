package com.example.proyectocitas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDTO {
    private Long id;
    private String day;
    private String startTime;
    private String endTime;
    
    public String getDay() {
        return this.day;
    }
    
    public String getStartTime() {
        return this.startTime;
    }
    
    public String getEndTime() {
        return this.endTime;
    }
}
