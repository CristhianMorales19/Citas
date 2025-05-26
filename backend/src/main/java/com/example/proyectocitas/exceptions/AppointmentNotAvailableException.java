package com.example.proyectocitas.exceptions;

public class AppointmentNotAvailableException extends RuntimeException {
    public AppointmentNotAvailableException(String message) {
        super(message);
    }
}
