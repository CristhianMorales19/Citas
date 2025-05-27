package com.example.proyectocitas.utils;

public class Constants {
    
    // Roles del sistema
    public static final String ROLE_PACIENTE = "paciente";
    public static final String ROLE_MEDICO = "medico";
    public static final String ROLE_ADMIN = "admin";
    
    // Estados de médicos
    public static final String DOCTOR_STATUS_PENDING = "PENDING";
    public static final String DOCTOR_STATUS_APPROVED = "APPROVED";
    public static final String DOCTOR_STATUS_REJECTED = "REJECTED";
    
    // Mensajes de error comunes
    public static final String ERROR_USER_EXISTS = "El nombre de usuario ya está en uso";
    public static final String ERROR_ROLE_NOT_FOUND = "Rol no encontrado: ";
    public static final String ERROR_USER_NOT_FOUND = "Usuario no encontrado";
    public static final String ERROR_INVALID_CREDENTIALS = "Credenciales inválidas";
    
    // Configuración JWT
    public static final long JWT_EXPIRATION = 86400000; // 24 horas
    
    private Constants() {
        // Clase de utilidad - constructor privado
    }
}
