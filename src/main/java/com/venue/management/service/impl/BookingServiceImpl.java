package com.venue.management.service.impl;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.repository.BookingRepository;
import com.venue.management.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public List<Booking> getCustomerBookings(User user) {
        return bookingRepository.findByCustomer(user);
    }

    @Override
    public Booking createBooking(Booking booking) {
        // Simple double booking check
        List<Booking> allBookings = bookingRepository.findAll();
        boolean conflict = allBookings.stream()
                .anyMatch(b -> b.getVenue().getVenueId().equals(booking.getVenue().getVenueId()) &&
                               b.getEventDate().equals(booking.getEventDate()) && 
                               !"CANCELLED".equals(b.getStatus()));
        
        if (conflict) {
            throw new RuntimeException("Venue is already booked for this date.");
        }
        booking.setStatus("PENDING");
        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}
