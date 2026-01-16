package com.venue.management.repository;

import com.venue.management.entity.Role;
import com.venue.management.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Extends JpaRepository to provide CRUD and pagination/sorting capabilities.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
	boolean existsByRole(Role admin);
}
