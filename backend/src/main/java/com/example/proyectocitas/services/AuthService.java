package com.example.proyectocitas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.proyectocitas.dto.AuthResponse;
import com.example.proyectocitas.dto.LoginRequest;
import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.dto.UserDTO;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.security.JwtService;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, 
                      PasswordEncoder passwordEncoder, JwtService jwtService, 
                      AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("El nombre de usuario ya está en uso")
                    .build();
        }

        // Primero asegurarse de que los roles existan
        try {
            initRoles();
        } catch (Exception e) {
            // Ignorar si ya existen
            log.warn("Error al inicializar roles: {}", e.getMessage());
        }
        
        // Obtener el rol predeterminado (paciente)
        String roleName = "paciente"; // Siempre usar paciente como valor predeterminado
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            roleName = request.getRole().trim();
        }

        log.info("Buscando rol: {}", roleName);
        
        // Primero crear todos los roles básicos si no existen ya
        Role adminRole = ensureRoleExists("admin");
        Role medicoRole = ensureRoleExists("medico");
        Role pacienteRole = ensureRoleExists("paciente");
        
        // Seleccionar el rol apropiado basado en el nombre (defaulting a paciente)
        Role role;
        if ("admin".equals(roleName)) {
            role = adminRole;
        } else if ("medico".equals(roleName)) {
            role = medicoRole;
        } else {
            // Por defecto, asignar el rol de paciente
            role = pacienteRole;
        }
        
        log.info("Usuario será creado con el rol: {}", role.getName());

        // Crear el usuario
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);

        // Generar token JWT
        String jwtToken = jwtService.generateToken(user);

        // Crear DTO para la respuesta
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().getName())
                .build();

        return AuthResponse.builder()
                .success(true)
                .message("Usuario registrado correctamente")
                .token(jwtToken)
                .user(userDTO)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Intento de inicio de sesión para usuario: {}", request.getNombreUsuario());
        
        try {
            // Verificar si el usuario existe primero
            User user = userRepository.findByUsername(request.getNombreUsuario())
                    .orElseThrow(() -> {
                        log.warn("Usuario no encontrado: {}", request.getNombreUsuario());
                        return new IllegalArgumentException("Usuario no encontrado");
                    });
            
            if (!user.isEnabled()) {
                log.warn("Intento de inicio de sesión para usuario deshabilitado: {}", request.getNombreUsuario());
                return AuthResponse.builder()
                        .success(false)
                        .message("La cuenta está deshabilitada")
                        .build();
            }
            
            // Intentar autenticar
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getNombreUsuario(),
                                request.getContrasena()
                        )
                );
            } catch (Exception e) {
                log.warn("Error de autenticación para usuario {}: {}", request.getNombreUsuario(), e.getMessage());
                return AuthResponse.builder()
                        .success(false)
                        .message("Contraseña incorrecta")
                        .build();
            }

            // Si llegamos aquí, la autenticación fue exitosa
            String jwtToken = jwtService.generateToken(user);
            log.info("Inicio de sesión exitoso para usuario: {}", request.getNombreUsuario());

            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .role(user.getRole().getName())
                    .build();

            return AuthResponse.builder()
                    .success(true)
                    .message("Inicio de sesión exitoso")
                    .token(jwtToken)
                    .user(userDTO)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            log.warn("Error en login: {}", e.getMessage());
            return AuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado durante el login", e);
            return AuthResponse.builder()
                    .success(false)
                    .message("Error en el servidor: " + e.getMessage())
                    .build();
        }
    }

    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().getName())
                .build();
    }
    
    /**
     * Inicializa los roles básicos del sistema si no existen
     */
    private void initRoles() {
        log.info("Inicializando roles básicos...");
        
        // Usar el método ensureRoleExists para cada rol básico
        ensureRoleExists("admin");
        ensureRoleExists("medico");
        ensureRoleExists("paciente");
    }
    
    /**
     * Garantiza que un rol con el nombre especificado exista en la base de datos
     * Si no existe, lo crea y lo guarda
     * 
     * @param roleName El nombre del rol
     * @return El rol existente o recién creado
     */
    private Role ensureRoleExists(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    log.info("Rol '{}' no encontrado, creando uno nuevo", roleName);
                    Role newRole = Role.builder().name(roleName).build();
                    return roleRepository.save(newRole);
                });
    }
}
