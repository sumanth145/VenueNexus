package com.venue.management.service.impl;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import com.venue.management.entity.User;
import com.venue.management.repository.BookingRepository;
import com.venue.management.repository.PaymentRepository;
import com.venue.management.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public Page<Payment> getAllPayments(String status, Pageable pageable) {
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            return paymentRepository.findByPaymentStatus(status, pageable);
        }
        return paymentRepository.findAll(pageable);
    }

    @Override
    public Page<Payment> getUserPayments(User user, String status, Pageable pageable) {
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            return paymentRepository.findByBooking_User_UserIdAndPaymentStatus(user.getUserId(), status, pageable);
        }
        return paymentRepository.findByBooking_User_UserId(user.getUserId(), pageable);
    }

    @Override
    @Transactional
    public void refundPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Payment payment = paymentRepository.findByBooking(booking).orElse(null);
        
        if (payment != null && "SUCCESS".equals(payment.getPaymentStatus())) {
            payment.setPaymentStatus("REFUNDED");
            paymentRepository.save(payment);
        }
    }

    @Override
    public double getTotalEarnings() {
        Double total = paymentRepository.sumOfPayments();
        return total != null ? total : 0.0;
    }

    @Override
    public long getSuccessfulPaymentsCount() {
        return paymentRepository.countByPaymentStatus("SUCCESS");
    }

    @Override
    public long getPendingPaymentsCount() {
        return paymentRepository.countByPaymentStatus("PENDING");
    }

    @Override
    public long getRefundedPaymentsCount() {
        return paymentRepository.countByPaymentStatus("REFUNDED");
    }

    @Override
    public double getTotalRefundedAmount() {
        Double total = paymentRepository.sumRefundedPayments();
        return total != null ? total : 0.0;
    }
}
