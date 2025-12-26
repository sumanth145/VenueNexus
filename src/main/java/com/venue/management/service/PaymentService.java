package com.venue.management.service;

import com.venue.management.entity.Payment;

public interface PaymentService {
    Payment processPayment(Payment payment);
    Payment getPaymentById(Long id);
}
