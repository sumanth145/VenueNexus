package com.venue.management.service.impl;

import com.venue.management.entity.Venue;
import com.venue.management.repository.VenueRepository;
import com.venue.management.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VenueServiceImpl implements VenueService {

    @Autowired
    private VenueRepository venueRepository;

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
    public void deleteVenue(Long id) {
        venueRepository.deleteById(id);
    }

    @Override
    public List<Venue> getAvailableVenues() {
        return venueRepository.findByStatus("AVAILABLE");
    }
}
