package com.example.proyectocitas.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    private final UserRepository userRepository;
    
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First try to find by username normally
        try {
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        } catch (Exception e) {
            // If there's a NonUniqueResultException, handle duplicates
            log.warn("Multiple users found for username: {}. Attempting to resolve duplicates.", username);
            
            // Get all users with this username
            List<User> duplicateUsers = userRepository.findAllByUsername(username);
            
            if (duplicateUsers.isEmpty()) {
                throw new UsernameNotFoundException("Usuario no encontrado: " + username);
            }
            
            // Return the first user and log the issue
            User userToReturn = duplicateUsers.get(0);
            log.error("Found {} duplicate users for username: {}. Returning user with ID: {}. This needs to be cleaned up!", 
                     duplicateUsers.size(), username, userToReturn.getId());
            
            return userToReturn;
        }
    }
}
