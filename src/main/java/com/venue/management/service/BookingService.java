package com.venue.management.service;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import java.util.List;

public interface BookingService {
    List<Booking> getAllBookings();
    List<Booking> getCustomerBookings(User user);
    Booking createBooking(Booking booking);
    Booking updateStatus(Long id, String status);
}
