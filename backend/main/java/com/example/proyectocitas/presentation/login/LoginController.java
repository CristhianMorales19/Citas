package com.example.proyectocitas.presentation.login;

import com.example.proyectocitas.logic.CitasService;
import com.example.proyectocitas.logic.Medico;
import com.example.proyectocitas.logic.Paciente;
import com.example.proyectocitas.logic.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller("login")
@RequestMapping("/login")
public class LoginController {

    @RestController
    @RequestMapping("/api/auth")
    public class AuthRestController {

        @PostMapping("/register")
        public ResponseEntity<AuthResponse> register(@RequestBody RegistrationRequest request) {
            try {
                // Validar contraseñas
                if (!request.getPassword().equals(request.getConfirmPassword())) {
                    return ResponseEntity.badRequest().body(new AuthResponse(false, "Las contraseñas no coinciden"));
                }

                // Crear usuario
                Usuario usuario = new Usuario();
                usuario.setNombre(request.getUsername());
                usuario.setContrasena(new BCryptPasswordEncoder().encode(request.getPassword()));
                usuario.setRol(request.getRole());
                usuario.setId(null);

                // Guardar usuario
                usuario = service.saveUsuario(usuario);

                // Crear y guardar usuario específico (paciente o médico)
                if (request.getRole().equals("paciente")) {
                    Paciente paciente = new Paciente();
                    paciente.setIdUsuario(usuario);
                    service.savePaciente(paciente);
                } else if (request.getRole().equals("medico")) {
                    Medico medico = new Medico();
                    medico.setIdUsuario(usuario);
                    medico.setAutorizado(false);
                    medico.setPrimeraVez(true);
                    service.saveMedico(medico);
                }

                // Crear token
                String token = new BCryptPasswordEncoder().encode(request.getUsername() + System.currentTimeMillis());
                
                return ResponseEntity.ok(new AuthResponse(true, "Registro exitoso", token, usuario));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthResponse(false, "El usuario ya existe"));
            }
        }

        static class RespuestaAutenticacion {
            private boolean exito;
            private String mensaje;
            private String token;
            private Usuario usuario;

            public RespuestaAutenticacion(boolean exito, String mensaje) {
                this.exito = exito;
                this.mensaje = mensaje;
            }

            public RespuestaAutenticacion(boolean exito, String mensaje, String token, Usuario usuario) {
                this.exito = exito;
                this.mensaje = mensaje;
                this.token = token;
                this.usuario = usuario;
            }

            public boolean isExito() { return exito; }
            public String getMensaje() { return mensaje; }
            public String getToken() { return token; }
            public Usuario getUsuario() { return usuario; }
        }

        static class SolicitudRegistro {
            private String nombreUsuario;
            private String contrasena;
            private String confirmarContrasena;
            private String rol;

            // Getters and setters
            public String getNombreUsuario() { return nombreUsuario; }
            public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
            public String getContrasena() { return contrasena; }
            public void setContrasena(String contrasena) { this.contrasena = contrasena; }
            public String getConfirmarContrasena() { return confirmarContrasena; }
            public void setConfirmarContrasena(String confirmarContrasena) { this.confirmarContrasena = confirmarContrasena; }
            public String getRol() { return rol; }
            public void setRol(String rol) { this.rol = rol; }
        }
    }

    @Autowired
    private CitasService service;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model, Principal authentication) {
        if (error != null) {
            model.addAttribute("errorMessage", switch (error) {
                case "no_autorizado" -> "No está autorizado para acceder.";
                case "no_encontrado" -> "Credenciales incorrectas. Inténtalo de nuevo.";
                default -> null;
            });
        }
        model.addAttribute("user", authentication != null ? service.getUsuarioByNombre(authentication.getName()) : null);
        return "presentation/login/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .ifPresent(auth -> new SecurityContextLogoutHandler().logout(request, response, auth));
        return "redirect:/";
    }

    @GetMapping("/registroPaciente")
    public String registroPaciente(Model model, Principal authentication) {
        model.addAttribute("paciente", new Paciente());
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("user", authentication != null ? service.getUsuarioByNombre(authentication.getName()) : null);
        return "/presentation/login/registroPaciente";
    }

    @PostMapping("/registrarPaciente")
    public String registrarPaciente(@ModelAttribute Paciente paciente, @ModelAttribute Usuario usuario,
                                    @RequestParam("confirmContrasena") String confirmContrasena, Model model) {
        if (!usuario.getContrasena().equals(confirmContrasena)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "/presentation/login/registroPaciente";
        }
        usuario.setRol("paciente");
        usuario.setContrasena(new BCryptPasswordEncoder().encode(usuario.getContrasena()));
        usuario.setId(null);
        try {
            usuario = service.saveUsuario(usuario);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "El usuario ya existe");
            return "/presentation/login/registroPaciente";
        }
        paciente.setIdUsuario(usuario);
        service.savePaciente(paciente);
        return "redirect:/login";
    }

    @GetMapping("/registroMedico")
    public String registroMedico(Model model, Principal authentication) {
        model.addAttribute("medico", new Medico());
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("user", authentication != null ? service.getUsuarioByNombre(authentication.getName()) : null);
        return "/presentation/login/registroMedico";
    }

    @PostMapping("/registrarMedico")
    public String registrarMedico(@ModelAttribute Medico medico, @ModelAttribute Usuario usuario,
                                  @RequestParam("confirmContrasena") String confirmContrasena,
                                  @RequestParam(value = "diasAtencion", required = false) List<String> diasAtencion,
                                  @RequestParam(value = "foto", required = false) MultipartFile foto,
                                  Model model) throws IOException {
        if (!usuario.getContrasena().equals(confirmContrasena)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "/presentation/login/registroMedico";
        }
        usuario.setRol("medico");
        usuario.setContrasena(new BCryptPasswordEncoder().encode(usuario.getContrasena()));
        usuario.setId(null);
        try {
            usuario = service.saveUsuario(usuario);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "El usuario ya existe");
            return "/presentation/login/registroMedico";
        }
        medico.setAutorizado(false);
        medico.setIdUsuario(usuario);
        medico.setPrimeraVez(true);
        service.saveMedico(medico);

        String directorioBase = "C:/ImagenesProyecto1/";
        File directorio = new File(directorioBase);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String rutaImagen = directorioBase + medico.getId() + ".jpg";
        if (foto != null && !foto.isEmpty()) {
            foto.transferTo(new File(rutaImagen));
        }

        if (diasAtencion != null && !diasAtencion.isEmpty()) {
            service.saveAllHorarios(diasAtencion, medico);
        }
        return "redirect:/login";
    }

    @GetMapping("/about")
    public String about() {
        return "/about";
    }

    @GetMapping("/foto/{id}")
    @ResponseBody
    public ResponseEntity<Resource> foto(@PathVariable Integer id) {
        String directorioBase = "C:/ImagenesProyecto1/";
        String rutaImagen = directorioBase + id + ".jpg";
        Path path = Paths.get(rutaImagen);

        File file = path.toFile();
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
