package com.example.proyectocitas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String role;
    
    public Long getId() {
        return this.id;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getRole() {
        return this.role;
    }
    
    // Builder method
    public static UserDTOBuilder builder() {
        return new UserDTOBuilder();
    }
    
    // Builder class
    public static class UserDTOBuilder {
        private UserDTO dto = new UserDTO();
        
        public UserDTOBuilder id(Long id) {
            dto.id = id;
            return this;
        }
        
        public UserDTOBuilder username(String username) {
            dto.username = username;
            return this;
        }
        
        public UserDTOBuilder name(String name) {
            dto.name = name;
            return this;
        }
        
        public UserDTOBuilder role(String role) {
            dto.role = role;
            return this;
        }
        
        public UserDTO build() {
            return dto;
        }
    }
}
