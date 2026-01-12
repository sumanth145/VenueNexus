package com.venue.management.service;

import com.venue.management.entity.Venue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Venue operations.
 * Provides methods for managing venues including pagination, sorting, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
public interface VenueService {
    List<Venue> getAllVenues();
    Page<Venue> getAllVenues(String search, Pageable pageable);
    Optional<Venue> getVenueById(Long id);
    Venue saveVenue(Venue venue);
    void deleteVenue(Long id);
    List<Venue> getAvailableVenues();
}
