package com.venue.management.repository;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.entity.Venue; // Ensure Venue is imported
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Booking entity operations.
 * Extends JpaRepository to provide CRUD and pagination/sorting capabilities.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    
    
    
    // Add this method to resolve the undefined method error
    List<Booking> findByVenue(Venue venue);

    // Supporting methods for pagination and filtering
    Page<Booking> findByUser(User user, Pageable pageable);
    Page<Booking> findByStatus(String status, Pageable pageable);
    Page<Booking> findByUserAndStatus(User user, String status, Pageable pageable);
    
    // Search methods
    Page<Booking> findByVenue_VenueNameContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Booking> findByUser_UsernameContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Booking> findByStatusContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Booking>  findByVenue_VenueNameContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrStatusContainingIgnoreCase(
        String venueName, String username, String status, Pageable pageable);
}