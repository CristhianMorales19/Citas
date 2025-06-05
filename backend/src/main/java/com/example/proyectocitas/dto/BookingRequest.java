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
public class BookingRequest {

    private Long doctorId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String date;

    @JsonFormat(pattern = "HH:mm")
    private String time;

    private String notes;

    // Convert to LocalDate and LocalTime for internal processing
    public LocalDate getDateAsLocalDate() {
        return LocalDate.parse(this.date);
    }

    public LocalTime getTimeAsLocalTime() {
        return LocalTime.parse(this.time);
    }

    // Explicit getters in case Lombok doesn't work
    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
