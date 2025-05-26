package com.example.proyectocitas.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyectocitas.models.Doctor;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.DoctorRepository;
import com.example.proyectocitas.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRegistrationListener {
    private static final Logger log = LoggerFactory.getLogger(UserRegistrationListener.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    
    /**
     * Este método se ejecuta después de un registro exitoso en UserController,
     * ya que necesitamos crear un registro de Doctor para los usuarios con rol "medico"
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDoctorProfileIfNeeded(User user) {
        try {
            if (user != null && "medico".equals(user.getRole().getName())) {
                log.info("Creando perfil de médico para el usuario: {}", user.getUsername());
                
                // Verificar si ya existe un perfil de doctor para este usuario
                boolean exists = doctorRepository.findByUser(user).isPresent();
                
                if (!exists) {
                    // Crear un nuevo perfil de doctor con status PENDING
                    Doctor doctor = new Doctor();
                    doctor.setUser(user);
                    doctor.setStatus("PENDING");
                    doctor.setEspecialidad("General"); // Especialidad por defecto
                    doctor.setCostoConsulta(0.0);
                    doctor.setAppointmentDuration(30); // 30 minutos por defecto
                    doctor.setActivo(true);
                    doctor.setProfileConfigured(false);
                    
                    doctor = doctorRepository.save(doctor);
                    log.info("Perfil de médico creado exitosamente para: {}", user.getUsername());
                    return;
                } else {
                    log.info("El usuario ya tiene un perfil de médico: {}", user.getUsername());
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error al crear perfil de médico para el usuario {}: {}", 
                user != null ? user.getUsername() : "null", 
                e.getMessage(), 
                e);
            throw new RuntimeException("No se pudo crear el perfil de médico: " + e.getMessage(), e);
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
