package com.venue.management.config;

import com.venue.management.entity.Role;
import com.venue.management.entity.User;
import com.venue.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if any user with role ADMIN exists
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User();
            admin.setUsername("admin");
            // Encoding the default password for security
            admin.setPassword(passwordEncoder.encode("admin")); 
            admin.setEmail("admin@venue.com");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);
            System.out.println("Default Admin user created.");
        } else {
            System.out.println("Admin user already exists in the system.");
        }
    }
}