package com.venue.management.service.impl;

import com.venue.management.entity.Venue;
import com.venue.management.repository.BookingRepository;
import com.venue.management.repository.VenueRepository;
import com.venue.management.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VenueServiceImpl implements VenueService {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    @Override
    public Optional<Venue> getVenueById(Long id) {
        return venueRepository.findById(id);
    }

    @Override
    public Venue saveVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        
        // Delete all bookings associated with this venue first
        bookingRepository.findByVenue(venue).forEach(bookingRepository::delete);
        
        // Now delete the venue
        venueRepository.deleteById(id);
    }

    @Override
    public List<Venue> getAvailableVenues() {
        return venueRepository.findByStatus("AVAILABLE");
    }
}
