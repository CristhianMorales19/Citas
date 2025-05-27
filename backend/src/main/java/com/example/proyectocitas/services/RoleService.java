package com.example.proyectocitas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyectocitas.exceptions.RoleNotFoundException;
import com.example.proyectocitas.models.Role;
import com.example.proyectocitas.repositories.RoleRepository;
import com.example.proyectocitas.utils.Constants;

@Service
public class RoleService {
    
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    
    private final RoleRepository roleRepository;
    
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    
    /**
     * Inicializa los roles del sistema si no existen
     */
    @Transactional
    public void initializeRoles() {
        createRoleIfNotExists(Constants.ROLE_PACIENTE);
        createRoleIfNotExists(Constants.ROLE_MEDICO);
        createRoleIfNotExists(Constants.ROLE_ADMIN);
        log.info("Roles del sistema inicializados correctamente");
    }
    
    /**
     * Obtiene un rol por nombre, lanzando excepción si no existe
     */
    public Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseThrow(() -> new RoleNotFoundException(Constants.ERROR_ROLE_NOT_FOUND + roleName));
    }
    
    /**
     * Crea un rol si no existe
     */
    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.debug("Rol creado: {}", roleName);
        }
    }
}
