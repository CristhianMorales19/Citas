package com.example.proyectocitas.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    public String getValue() {
        return this.name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    // Custom builder method
    public static RoleBuilder builder() {
        return new RoleBuilder();
    }
    
    // Builder class
    public static class RoleBuilder {
        private Role role = new Role();
        
        public RoleBuilder id(Long id) {
            role.id = id;
            return this;
        }
        
        public RoleBuilder name(String name) {
            role.name = name;
            return this;
        }
        
        public Role build() {
            return role;
        }
    }
}
