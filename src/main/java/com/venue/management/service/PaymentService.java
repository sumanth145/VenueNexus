package com.venue.management.service;

import com.venue.management.entity.Payment;
import com.venue.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    Page<Payment> getAllPayments(String status, Pageable pageable);
    Page<Payment> getUserPayments(User user, String status, Pageable pageable);
    Payment processPayment(Payment payment);
    void refundPayment(Long bookingId);
    double getTotalEarnings();
    long getSuccessfulPaymentsCount();
    long getPendingPaymentsCount();
    long getRefundedPaymentsCount();
    double getTotalRefundedAmount();
	Payment getPaymentById(Long id);
}