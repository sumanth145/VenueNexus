package com.venue.management.service;

import com.venue.management.entity.Venue;
import com.venue.management.repository.BookingRepository;
import com.venue.management.repository.VenueRepository;
import com.venue.management.service.impl.VenueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VenueService.
 * Tests venue CRUD operations, pagination, sorting, and search functionality.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private VenueServiceImpl venueService;

    private Venue venue;

    @BeforeEach
    void setUp() {
        venue = new Venue();
        venue.setVenueId(1L);
        venue.setVenueName("Test Venue");
        venue.setLocation("Test Location");
        venue.setCapacity(100);
        venue.setPricePerDay(1000.0);
        venue.setStatus("AVAILABLE");
    }

    @Test
    void testSaveVenue_Success() {
        // Arrange
        when(venueRepository.save(any(Venue.class))).thenReturn(venue);

        // Act
        Venue result = venueService.saveVenue(venue);

        // Assert
        assertNotNull(result);
        assertEquals("Test Venue", result.getVenueName());
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    void testGetVenueById_Success() {
        // Arrange
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        // Act
        Optional<Venue> result = venueService.getVenueById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Venue", result.get().getVenueName());
    }

    @Test
    void testGetVenueById_NotFound() {
        // Arrange
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Venue> result = venueService.getVenueById(1L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllVenues() {
        // Arrange
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);
        when(venueRepository.findAll()).thenReturn(venues);

        // Act
        List<Venue> result = venueService.getAllVenues();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(venueRepository, times(1)).findAll();
    }

    @Test
    void testGetAllVenues_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);
        Page<Venue> page = new PageImpl<>(venues, pageable, 1);
        when(venueRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Venue> result = venueService.getAllVenues(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(venueRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetAvailableVenues() {
        // Arrange
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);
        when(venueRepository.findByStatus("AVAILABLE")).thenReturn(venues);

        // Act
        List<Venue> result = venueService.getAvailableVenues();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(venueRepository, times(1)).findByStatus("AVAILABLE");
    }

    @Test
    void testDeleteVenue_Success() {
        // Arrange
        List<com.venue.management.entity.Booking> bookings = new ArrayList<>();
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(bookingRepository.findByVenue(venue)).thenReturn(bookings);
        doNothing().when(venueRepository).deleteById(1L);

        // Act
        venueService.deleteVenue(1L);

        // Assert
        verify(venueRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteVenue_NotFound() {
        // Arrange
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            venueService.deleteVenue(1L);
        });
        verify(venueRepository, never()).deleteById(anyLong());
    }
}

