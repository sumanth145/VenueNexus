package com.venue.management.service.impl;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import com.venue.management.repository.BookingRepository;
import com.venue.management.repository.PaymentRepository;
import com.venue.management.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public Payment processPayment(Payment payment) {
        Booking booking = bookingRepository.findById(payment.getBooking().getBookingId()).orElseThrow();
        
        // Calculate total amount based on number of days
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            booking.getEventDate(), 
            booking.getEndDate()
        ) + 1; // +1 to include both start and end dates
        double totalAmount = booking.getVenue().getPricePerDay() * days;
        
        // Simulate payment success
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentAmount(totalAmount);
        
        // Update booking status
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElseThrow();
    }
}
