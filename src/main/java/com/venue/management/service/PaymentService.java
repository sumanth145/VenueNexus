package com.venue.management.service;

import com.venue.management.entity.Payment;
import com.venue.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Payment operations.
 * Provides methods for managing payments including pagination, sorting, filtering, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
public interface PaymentService {
    Page<Payment> getAllPayments(String status, String search, Pageable pageable);
    Page<Payment> getUserPayments(User user, String status, String search, Pageable pageable);
    Payment processPayment(Payment payment);
    void refundPayment(Long bookingId);
    double getTotalEarnings();
    long getSuccessfulPaymentsCount();
    long getPendingPaymentsCount();
    long getRefundedPaymentsCount();
    double getTotalRefundedAmount();
	Payment getPaymentById(Long id);
}