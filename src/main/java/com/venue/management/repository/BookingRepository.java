package com.venue.management.repository;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.entity.Venue; // Ensure Venue is imported
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    
    
    
    // Add this method to resolve the undefined method error
    List<Booking> findByVenue(Venue venue);

    // Supporting methods for pagination and filtering
    Page<Booking> findByUser(User user, Pageable pageable);
    Page<Booking> findByStatus(String status, Pageable pageable);
    Page<Booking> findByUserAndStatus(User user, String status, Pageable pageable);
}