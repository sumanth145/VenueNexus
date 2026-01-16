package com.venue.management.repository;

import com.venue.management.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Venue entity operations.
 * Extends JpaRepository to provide CRUD and pagination/sorting capabilities.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByStatus(String status);
    
    // Pagination and sorting methods
    Page<Venue> findByStatus(String status, Pageable pageable);
    
    // Search methods
    Page<Venue> findByVenueNameContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Venue> findByLocationContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Venue> findByStatusContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Venue> findByVenueNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrStatusContainingIgnoreCase(
    String venueName, String location, String status, Pageable pageable);
}
