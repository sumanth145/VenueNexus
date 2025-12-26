package com.venue.management.service;

import com.venue.management.entity.User;
import java.util.Optional;

/**
 * Service interface for User Management.
 */
public interface UserService {
    User registerUser(User user);
    Optional<User> findByUsername(String username);
}
