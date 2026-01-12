package com.venue.management.service;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Booking operations.
 * Provides methods for managing bookings including pagination, sorting, filtering, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
public interface BookingService {
    // Updated to support paging, status filtering, and search
    Page<Booking> getAllBookings(String status, String search, Pageable pageable);
    Page<Booking> getCustomerBookings(User user, String status, String search, Pageable pageable);
    
    List<Booking> getTotalBookings();
    
    // Original methods remain for internal logic
    Booking createBooking(Booking booking);
    Booking updateStatus(Long id, String status);
    Booking getBookingById(Long id);
}