package com.example.proyectocitas.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyectocitas.models.User;
import com.example.proyectocitas.repositories.UserRepository;

@Service
@Order(1) // Execute before DataInitializer
public class DatabaseCleanupService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseCleanupService.class);
    private final UserRepository userRepository;

    public DatabaseCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando limpieza de usuarios duplicados...");
        cleanupDuplicateUsers();
        log.info("Limpieza de usuarios duplicados completada.");
    }

    @Transactional
    public void cleanupDuplicateUsers() {
        try {
            // Get all users
            List<User> allUsers = userRepository.findAll();
            
            // Group users by username
            Map<String, List<User>> usersByUsername = allUsers.stream()
                .collect(Collectors.groupingBy(User::getUsername));
            
            // Find and clean up duplicates
            int totalDuplicatesRemoved = 0;
            
            for (Map.Entry<String, List<User>> entry : usersByUsername.entrySet()) {
                String username = entry.getKey();
                List<User> usersWithSameUsername = entry.getValue();
                
                if (usersWithSameUsername.size() > 1) {
                    log.warn("Found {} users with username: {}", usersWithSameUsername.size(), username);
                    
                    // Keep the first user (usually the oldest by ID) and delete the rest
                    User userToKeep = usersWithSameUsername.get(0);
                    List<User> usersToDelete = usersWithSameUsername.subList(1, usersWithSameUsername.size());
                    
                    log.info("Keeping user ID: {} for username: {}", userToKeep.getId(), username);
                    
                    for (User userToDelete : usersToDelete) {
                        log.info("Deleting duplicate user ID: {} for username: {}", userToDelete.getId(), username);
                        userRepository.delete(userToDelete);
                        totalDuplicatesRemoved++;
                    }
                }
            }
            
            if (totalDuplicatesRemoved > 0) {
                log.info("Successfully removed {} duplicate users from the database.", totalDuplicatesRemoved);
            } else {
                log.info("No duplicate users found. Database is clean.");
            }
            
        } catch (Exception e) {
            log.error("Error during duplicate user cleanup: {}", e.getMessage(), e);
            // Don't rethrow the exception to allow application to continue starting
        }
    }
}
