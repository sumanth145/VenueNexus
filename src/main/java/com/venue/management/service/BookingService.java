package com.venue.management.service;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    // Updated to support paging and status filtering
    Page<Booking> getAllBookings(String status, Pageable pageable);
    Page<Booking> getCustomerBookings(User user, String status, Pageable pageable);
    
    List<Booking> getTotalBookings();
    
    // Original methods remain for internal logic
    Booking createBooking(Booking booking);
    Booking updateStatus(Long id, String status);
    Booking getBookingById(Long id);
}