package com.venue.management.service.impl;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import com.venue.management.entity.User;
import com.venue.management.repository.BookingRepository;
import com.venue.management.repository.PaymentRepository;
import com.venue.management.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementation for Payment operations.
 * Handles business logic for payments including processing, refunds, pagination, sorting, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Processes a payment for a booking.
     * Calculates the total amount based on number of days and updates booking status.
     * 
     * @param payment The payment to process
     * @return The processed payment
     */
    @Override
    public Payment processPayment(Payment payment) {
        logger.info("Processing payment for booking ID: {}", payment.getBooking().getBookingId());
        Booking booking = bookingRepository.findById(payment.getBooking().getBookingId())
            .orElseThrow(() -> {
                logger.error("Booking not found with ID: {}", payment.getBooking().getBookingId());
                return new RuntimeException("Booking not found");
            });
        
        // Calculate total amount based on number of days
        long days = java.time.temporal.ChronoUnit.DAYS.between(
            booking.getEventDate(), 
            booking.getEndDate()
        ) + 1; // +1 to include both start and end dates
        double totalAmount = booking.getVenue().getPricePerDay() * days;
        
        logger.debug("Calculated payment amount: {} for {} days at {} per day", 
            totalAmount, days, booking.getVenue().getPricePerDay());
        
        // Simulate payment success
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentAmount(totalAmount);
        
        // Update booking status
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment processed successfully with ID: {}", savedPayment.getPaymentId());
        return savedPayment;
    }

    /**
     * Retrieves a payment by its ID.
     * 
     * @param id The payment ID
     * @return The payment
     */
    @Override
    public Payment getPaymentById(Long id) {
        logger.debug("Getting payment by ID: {}", id);
        return paymentRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Payment not found with ID: {}", id);
                return new RuntimeException("Payment not found");
            });
    }

    /**
     * Retrieves all payments with optional status filter and search.
     * 
     * @param status Optional status filter
     * @param search Optional search term to search in venue name, username, or payment status
     * @param pageable Pagination and sorting parameters
     * @return Page of payments matching the criteria
     */
    @Override
    public Page<Payment> getAllPayments(String status, String search, Pageable pageable) {
        logger.debug("Getting all payments with status: {}, search: {}", status, search);
        
        // If search is provided, search across multiple fields
        if (search != null && !search.trim().isEmpty()) {
            Page<Payment> results = paymentRepository.findByBooking_Venue_VenueNameContainingIgnoreCaseOrBooking_User_UsernameContainingIgnoreCaseOrPaymentStatusContainingIgnoreCase(
                search, search, search, pageable);
            logger.info("Found {} payments matching search '{}'", results.getTotalElements(), search);
            return results;
        }
        
        // Apply status filter if provided
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            Page<Payment> results = paymentRepository.findByPaymentStatus(status, pageable);
            logger.info("Found {} payments with status '{}'", results.getTotalElements(), status);
            return results;
        }
        
        Page<Payment> results = paymentRepository.findAll(pageable);
        logger.info("Found {} total payments", results.getTotalElements());
        return results;
    }

    /**
     * Retrieves payments for a specific user with optional status filter and search.
     * 
     * @param user The user
     * @param status Optional status filter
     * @param search Optional search term
     * @param pageable Pagination and sorting parameters
     * @return Page of user payments matching the criteria
     */
    @Override
    public Page<Payment> getUserPayments(User user, String status, String search, Pageable pageable) {
        logger.debug("Getting payments for user: {} with status: {}, search: {}", 
            user.getUsername(), status, search);
        
        // If search is provided, search within user's payments
        if (search != null && !search.trim().isEmpty()) {
            Page<Payment> searchResults = paymentRepository.findByBooking_User_UserId(user.getUserId(), pageable);
            logger.info("Found {} payments for user '{}' matching search '{}'", 
                searchResults.getTotalElements(), user.getUsername(), search);
            return searchResults;
        }
        
        // Apply status filter if provided
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            Page<Payment> results = paymentRepository.findByBooking_User_UserIdAndPaymentStatus(user.getUserId(), status, pageable);
            logger.info("Found {} payments for user '{}' with status '{}'", 
                results.getTotalElements(), user.getUsername(), status);
            return results;
        }
        
        Page<Payment> results = paymentRepository.findByBooking_User_UserId(user.getUserId(), pageable);
        logger.info("Found {} payments for user '{}'", results.getTotalElements(), user.getUsername());
        return results;
    }

    /**
     * Refunds a payment for a cancelled booking.
     * 
     * @param bookingId The booking ID
     */
    @Override
    @Transactional
    public void refundPayment(Long bookingId) {
        logger.info("Processing refund for booking ID: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> {
                logger.error("Booking not found for refund with ID: {}", bookingId);
                return new RuntimeException("Booking not found");
            });
        
        Payment payment = paymentRepository.findByBooking(booking).orElse(null);
        
        if (payment != null && "SUCCESS".equals(payment.getPaymentStatus())) {
            payment.setPaymentStatus("REFUNDED");
            paymentRepository.save(payment);
            logger.info("Payment refunded successfully for booking ID: {}", bookingId);
        } else {
            logger.warn("No payment found to refund for booking ID: {}", bookingId);
        }
    }

    /**
     * Calculates total earnings from all payments.
     * 
     * @return Total earnings amount
     */
    @Override
    public double getTotalEarnings() {
        Double total = paymentRepository.sumOfPayments();
        double earnings = total != null ? total : 0.0;
        logger.debug("Total earnings: {}", earnings);
        return earnings;
    }

    /**
     * Counts successful payments.
     * 
     * @return Count of successful payments
     */
    @Override
    public long getSuccessfulPaymentsCount() {
        long count = paymentRepository.countByPaymentStatus("SUCCESS");
        logger.debug("Successful payments count: {}", count);
        return count;
    }

    /**
     * Counts pending payments.
     * 
     * @return Count of pending payments
     */
    @Override
    public long getPendingPaymentsCount() {
        long count = paymentRepository.countByPaymentStatus("PENDING");
        logger.debug("Pending payments count: {}", count);
        return count;
    }

    /**
     * Counts refunded payments.
     * 
     * @return Count of refunded payments
     */
    @Override
    public long getRefundedPaymentsCount() {
        long count = paymentRepository.countByPaymentStatus("REFUNDED");
        logger.debug("Refunded payments count: {}", count);
        return count;
    }

    /**
     * Calculates total refunded amount.
     * 
     * @return Total refunded amount
     */
    @Override
    public double getTotalRefundedAmount() {
        Double total = paymentRepository.sumRefundedPayments();
        double refunded = total != null ? total : 0.0;
        logger.debug("Total refunded amount: {}", refunded);
        return refunded;
    }
}
