package com.example.proyectocitas.models;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
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
    @Builder.Default
    private boolean enabled = true;
    
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getValue()));
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
    
    // Explicit getters/setters for fields that might have issues with Lombok
    public Long getId() {
        return id;
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
    
    // Custom builder method
    public static UserBuilder builder() {
        return new UserBuilder();
    }
    
    // Builder class
    public static class UserBuilder {
        private User user = new User();
        
        public UserBuilder id(Long id) {
            user.id = id;
            return this;
        }
        
        public UserBuilder username(String username) {
            user.username = username;
            return this;
        }
        
        public UserBuilder password(String password) {
            user.password = password;
            return this;
        }
        
        public UserBuilder name(String name) {
            user.name = name;
            return this;
        }
        
        public UserBuilder email(String email) {
            user.email = email;
            return this;
        }
        
        public UserBuilder enabled(boolean enabled) {
            user.enabled = enabled;
            return this;
        }
        
        public UserBuilder role(Role role) {
            user.role = role;
            return this;
        }
        
        public User build() {
            return user;
        }
    }
}
