package com.example.proyectocitas.models;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Doctor doctor;
    
    private String password;
    private String name;
    private String email;
    private boolean enabled = true;
    
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    // Constructor por defecto
    public User() {
        this.enabled = true;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        String roleName = this.role.getName();
        if (roleName == null || roleName.trim().isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        // Convert to uppercase for Spring Security compatibility
        String authority = "ROLE_" + roleName.toUpperCase();
        return List.of(new SimpleGrantedAuthority(authority));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }
    
    // Explicit getters/setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Role getRole() {
        return role;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Doctor getDoctor() {
        return doctor;
    }
    
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
    
    // Custom builder method
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    // Builder class
    public static class UserBuilder {
        private final User user;
        
        public UserBuilder() {
            this.user = new User();
        }
        
        public UserBuilder id(Long id) {
            user.setId(id);
            return this;
        }
        
        public UserBuilder username(String username) {
            user.setUsername(username);
            return this;
        }
        
        public UserBuilder password(String password) {
            user.setPassword(password);
            return this;
        }
        
        public UserBuilder name(String name) {
            user.setName(name);
            return this;
        }
        
        public UserBuilder email(String email) {
            user.setEmail(email);
            return this;
        }
        
        public UserBuilder enabled(boolean enabled) {
            user.setEnabled(enabled);
            return this;
        }
        
        public UserBuilder role(Role role) {
            user.setRole(role);
            return this;
        }
        
        public User build() {
            return user;
        }
    }
}
