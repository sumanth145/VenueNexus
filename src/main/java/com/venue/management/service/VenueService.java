package com.venue.management.service;

import com.venue.management.entity.Venue;
import java.util.List;
import java.util.Optional;

public interface VenueService {
    List<Venue> getAllVenues();
    Optional<Venue> getVenueById(Long id);
    Venue saveVenue(Venue venue);
    void deleteVenue(Long id);
    List<Venue> getAvailableVenues();
}
