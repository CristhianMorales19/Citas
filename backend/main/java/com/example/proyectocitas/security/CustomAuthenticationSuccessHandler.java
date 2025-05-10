package com.example.proyectocitas.security;

import com.example.proyectocitas.logic.CitasService;
import com.example.proyectocitas.logic.Medico;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private CitasService service;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        // Redirigir según el rol del usuario
        String targetUrl = determineTargetUrl(authentication);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        
        switch (role) {
            case "ROLE_admin":
                return "/admin/dashboard";
            case "ROLE_Doctor":
                return "/medicos/dashboard";
            case "ROLE_paciente":
                return "/pacientes/dashboard";
            default:
                return "/";
        }
    }
}
