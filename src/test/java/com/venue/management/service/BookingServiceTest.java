package com.venue.management.service;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.entity.Venue;
import com.venue.management.repository.BookingRepository;
import com.venue.management.service.impl.BookingServiceImpl;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService.
 * Tests booking creation, status updates, pagination, sorting, and search functionality.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking booking;
    private User user;
    private Venue venue;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        venue = new Venue();
        venue.setVenueId(1L);
        venue.setVenueName("Test Venue");
        venue.setPricePerDay(1000.0);

        booking = new Booking();
        booking.setBookingId(1L);
        booking.setUser(user);
        booking.setVenue(venue);
        booking.setEventDate(LocalDate.now());
        booking.setEndDate(LocalDate.now().plusDays(2));
        booking.setStatus("PENDING");
    }

    @Test
    void testCreateBooking_Success() {
        // Arrange
        List<Booking> existingBookings = new ArrayList<>();
        when(bookingRepository.findAll()).thenReturn(existingBookings);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Act
        Booking result = bookingService.createBooking(booking);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_WithConflict() {
        // Arrange
        Booking conflictingBooking = new Booking();
        conflictingBooking.setVenue(venue);
        conflictingBooking.setEventDate(LocalDate.now());
        conflictingBooking.setEndDate(LocalDate.now().plusDays(2));
        conflictingBooking.setStatus("CONFIRMED");

        List<Booking> existingBookings = new ArrayList<>();
        existingBookings.add(conflictingBooking);
        when(bookingRepository.findAll()).thenReturn(existingBookings);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(booking);
        });
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testUpdateStatus_Success() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Act
        Booking result = bookingService.updateStatus(1L, "CONFIRMED");

        // Assert
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testUpdateStatus_WithCancellation() {
        // Arrange
        booking.setStatus("CONFIRMED");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        doNothing().when(paymentService).refundPayment(1L);

        // Act
        Booking result = bookingService.updateStatus(1L, "CANCELLED");

        // Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(paymentService, times(1)).refundPayment(1L);
    }

    @Test
    void testGetBookingById_Success() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        // Act
        Booking result = bookingService.getBookingById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
    }

    @Test
    void testGetBookingById_NotFound() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookingService.getBookingById(1L);
        });
    }

    @Test
    void testGetAllBookings_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings, pageable, 1);
        when(bookingRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Booking> result = bookingService.getAllBookings(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookingRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllBookings_WithStatusFilter() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings, pageable, 1);
        when(bookingRepository.findByStatus(eq("PENDING"), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Booking> result = bookingService.getAllBookings("PENDING", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookingRepository, times(1)).findByStatus(eq("PENDING"), any(Pageable.class));
    }

    @Test
    void testGetCustomerBookings_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings, pageable, 1);
        when(bookingRepository.findByUser(eq(user), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Booking> result = bookingService.getCustomerBookings(user, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookingRepository, times(1)).findByUser(eq(user), any(Pageable.class));
    }
}

