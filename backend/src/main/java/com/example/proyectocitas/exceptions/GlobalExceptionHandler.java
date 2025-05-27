package com.example.proyectocitas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.proyectocitas.dto.AuthResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<AuthResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.warn("Usuario ya existe: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(AuthResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build());
    }
    
    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<AuthResponse> handleRoleNotFound(RoleNotFoundException ex) {
        log.error("Rol no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(AuthResponse.builder()
                .success(false)
                .message("Error interno del servidor")
                .build());
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Credenciales inválidas: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(AuthResponse.builder()
                .success(false)
                .message("Credenciales inválidas")
                .build());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(AuthResponse.builder()
                .success(false)
                .message("Error interno del servidor")
                .build());
    }
}
