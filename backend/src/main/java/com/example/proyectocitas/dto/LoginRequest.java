package com.example.proyectocitas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String nombreUsuario;
    private String contrasena;
    
    public String getNombreUsuario() {
        return this.nombreUsuario;
    }
    
    public String getContrasena() {
        return this.contrasena;
    }
}
