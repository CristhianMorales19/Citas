package com.example.proyectocitas.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationListener {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    
    /**
     * Este método se ejecuta después de un registro exitoso en UserController,
     * ya que necesitamos crear un registro de Doctor para los usuarios con rol "medico"
     */
    @Transactional
    public void createDoctorProfileIfNeeded(User user) {
        try {
            if (user != null && "medico".equals(user.getRole().getName())) {
                log.info("Creando perfil de médico para el usuario: {}", user.getUsername());
                
                // Verificar si ya existe un perfil de doctor para este usuario
                boolean exists = doctorRepository.findByUser(user).isPresent();
                
                if (!exists) {
                    // Crear un nuevo perfil de doctor con status PENDING
                    Doctor doctor = Doctor.builder()
                            .user(user)
                            .status("PENDING")
                            .consultationCost(0.0)
                            .appointmentDuration(30) // Valor por defecto de 30 minutos
                            .build();
                    
                    doctorRepository.save(doctor);
                    log.info("Perfil de médico creado exitosamente para: {}", user.getUsername());
                } else {
                    log.info("El usuario ya tiene un perfil de médico: {}", user.getUsername());
                }
            }
        } catch (Exception e) {
            log.error("Error al crear perfil de médico: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Este método también escucha eventos de autenticación exitosa para crear perfiles de médico
     * si aún no existen para usuarios con rol médico
     */
    @EventListener
    @Transactional
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            if (event.getAuthentication().getPrincipal() instanceof User) {
                User user = (User) event.getAuthentication().getPrincipal();
                createDoctorProfileIfNeeded(user);
            }
        } catch (Exception e) {
            log.error("Error en evento de autenticación exitosa: {}", e.getMessage(), e);
        }
    }
}
