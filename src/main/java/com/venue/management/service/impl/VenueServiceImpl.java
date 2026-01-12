package com.venue.management.service.impl;

import com.venue.management.entity.Venue;
import com.venue.management.repository.BookingRepository;
import com.venue.management.repository.VenueRepository;
import com.venue.management.service.VenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Venue operations.
 * Handles business logic for venues including CRUD operations, pagination, sorting, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Service
public class VenueServiceImpl implements VenueService {

    private static final Logger logger = LoggerFactory.getLogger(VenueServiceImpl.class);

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Retrieves all venues without pagination.
     * 
     * @return List of all venues
     */
    @Override
    public List<Venue> getAllVenues() {
        logger.debug("Getting all venues");
        List<Venue> venues = (List<Venue>) venueRepository.findAll();
        logger.info("Retrieved {} venues", venues.size());
        return venues;
    }

    /**
     * Retrieves all venues with optional search, with pagination and sorting.
     * 
     * @param search Optional search term to search in venue name, location, or status
     * @param pageable Pagination and sorting parameters
     * @return Page of venues matching the criteria
     */
    @Override
    public Page<Venue> getAllVenues(String search, Pageable pageable) {
        logger.debug("Getting all venues with search: {}", search);
        
        // If search is provided, search across multiple fields
        if (search != null && !search.trim().isEmpty()) {
            Page<Venue> results = venueRepository.findByVenueNameContainingIgnoreCaseOrLocationContainingIgnoreCaseOrStatusContainingIgnoreCase(
                search, search, search, pageable);
            logger.info("Found {} venues matching search '{}'", results.getTotalElements(), search);
            return results;
        }
        
        Page<Venue> results = venueRepository.findAll(pageable);
        logger.info("Found {} total venues", results.getTotalElements());
        return results;
    }

    /**
     * Retrieves a venue by its ID.
     * 
     * @param id The venue ID
     * @return Optional venue
     */
    @Override
    public Optional<Venue> getVenueById(Long id) {
        logger.debug("Getting venue by ID: {}", id);
        Optional<Venue> venue = venueRepository.findById(id);
        if (venue.isPresent()) {
            logger.debug("Venue found: {}", venue.get().getVenueName());
        } else {
            logger.warn("Venue not found with ID: {}", id);
        }
        return venue;
    }

    /**
     * Saves a venue (creates or updates).
     * 
     * @param venue The venue to save
     * @return The saved venue
     */
    @Override
    public Venue saveVenue(Venue venue) {
        if (venue.getVenueId() == null) {
            logger.info("Creating new venue: {}", venue.getVenueName());
        } else {
            logger.info("Updating venue ID: {}, name: {}", venue.getVenueId(), venue.getVenueName());
        }
        
        Venue savedVenue = venueRepository.save(venue);
        logger.info("Venue saved successfully with ID: {}", savedVenue.getVenueId());
        return savedVenue;
    }

    /**
     * Deletes a venue and all associated bookings.
     * 
     * @param id The venue ID
     */
    @Override
    @Transactional
    public void deleteVenue(Long id) {
        logger.info("Deleting venue ID: {}", id);
        
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Venue not found with ID: {}", id);
                    return new RuntimeException("Venue not found");
                });
        
        // Delete all bookings associated with this venue first
        List<com.venue.management.entity.Booking> bookings = bookingRepository.findByVenue(venue);
        logger.debug("Deleting {} bookings associated with venue ID: {}", bookings.size(), id);
        bookings.forEach(bookingRepository::delete);
        
        // Now delete the venue
        venueRepository.deleteById(id);
        logger.info("Venue {} deleted successfully", id);
    }

    /**
     * Retrieves all available venues.
     * 
     * @return List of available venues
     */
    @Override
    public List<Venue> getAvailableVenues() {
        logger.debug("Getting available venues");
        List<Venue> venues = venueRepository.findByStatus("AVAILABLE");
        logger.info("Found {} available venues", venues.size());
        return venues;
    }
}
