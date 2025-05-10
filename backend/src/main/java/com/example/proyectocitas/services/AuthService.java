package com.example.proyectocitas.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.proyectocitas.security.AppUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.proyectocitas.dto.AuthRequest;
import com.example.proyectocitas.dto.AuthResponse;
import com.example.proyectocitas.dto.RegisterRequest;
import com.example.proyectocitas.models.AppUser;
import com.example.proyectocitas.repositories.UserRepository;
import com.example.proyectocitas.util.JwtUtil;
import com.example.proyectocitas.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                      AppUserDetailsService userDetailsService,
                      JwtUtil jwtUtil,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
                )
            );
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails);
            
            return new AuthResponse(jwt, (AppUser) userDetails);
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            // Validate input
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            if (registerRequest.getName() == null || registerRequest.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (registerRequest.getRole() == null) {
                throw new IllegalArgumentException("Role is required");
            }
            String role = registerRequest.getRole().toLowerCase();
            if (!role.equals("medico") && !role.equals("paciente") && !role.equals("admin")) {
                throw new IllegalArgumentException("Invalid role. Must be one of: 'medico', 'paciente', 'admin'");
            }

            // Check if username already exists
            Optional<AppUser> existingUserOptional = userRepository.findByUsername(registerRequest.getUsername());
            if (existingUserOptional.isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }

            // Create and save user
            AppUser user = new AppUser(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getName(),
                Role.fromValue(role)
            );

            AppUser savedUser = userRepository.save(user);
            String jwtToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(savedUser.getUsername()));
            return new AuthResponse(jwtToken, savedUser);
        } catch (Exception e) {
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }
}
