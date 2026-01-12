package com.venue.management.service;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import com.venue.management.entity.User;
import com.venue.management.entity.Venue;
import com.venue.management.repository.BookingRepository;
import com.venue.management.repository.PaymentRepository;
import com.venue.management.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService.
 * Tests payment processing, refunds, pagination, sorting, and search functionality.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    private Payment payment;
    private Booking booking;
    private User user;
    private Venue venue;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");

        venue = new Venue();
        venue.setVenueId(1L);
        venue.setVenueName("Test Venue");
        venue.setPricePerDay(1000.0);

        booking = new Booking();
        booking.setBookingId(1L);
        booking.setUser(user);
        booking.setVenue(venue);
        booking.setEventDate(LocalDate.now());
        booking.setEndDate(LocalDate.now().plusDays(2));
        booking.setStatus("PENDING");

        payment = new Payment();
        payment.setPaymentId(1L);
        payment.setBooking(booking);
        payment.setPaymentAmount(3000.0);
        payment.setPaymentStatus("PENDING");
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        Payment result = paymentServiceImpl.processPayment(payment);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getPaymentStatus());
        assertEquals("CONFIRMED", booking.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testGetPaymentById_Success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        Payment result = paymentServiceImpl.getPaymentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getPaymentId());
    }

    @Test
    void testGetPaymentById_NotFound() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            paymentServiceImpl.getPaymentById(1L);
        });
    }

    @Test
    void testRefundPayment_Success() {
        // Arrange
        payment.setPaymentStatus("SUCCESS");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        paymentServiceImpl.refundPayment(1L);

        // Assert
        assertEquals("REFUNDED", payment.getPaymentStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testRefundPayment_NoPaymentFound() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking(booking)).thenReturn(Optional.empty());

        // Act
        paymentServiceImpl.refundPayment(1L);

        // Assert
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testGetAllPayments_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = new ArrayList<>();
        payments.add(payment);
        Page<Payment> page = new PageImpl<>(payments, pageable, 1);
        when(paymentRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<Payment> result = paymentServiceImpl.getAllPayments(null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetUserPayments_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = new ArrayList<>();
        payments.add(payment);
        Page<Payment> page = new PageImpl<>(payments, pageable, 1);
        when(paymentRepository.findByBooking_User_UserId(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act
        Page<Payment> result = paymentServiceImpl.getUserPayments(user, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository, times(1)).findByBooking_User_UserId(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetTotalEarnings() {
        // Arrange
        when(paymentRepository.sumOfPayments()).thenReturn(10000.0);

        // Act
        double earnings = paymentServiceImpl.getTotalEarnings();

        // Assert
        assertEquals(10000.0, earnings);
    }

    @Test
    void testGetSuccessfulPaymentsCount() {
        // Arrange
        when(paymentRepository.countByPaymentStatus("SUCCESS")).thenReturn(5L);

        // Act
        long count = paymentServiceImpl.getSuccessfulPaymentsCount();

        // Assert
        assertEquals(5L, count);
    }
}

