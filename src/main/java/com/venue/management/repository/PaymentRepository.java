package com.venue.management.repository;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 * Extends JpaRepository to provide CRUD and pagination/sorting capabilities.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);
    
    // Pagination & Filtering methods
    Page<Payment> findByBooking_User_UserId(Long userId, Pageable pageable);
    Page<Payment> findByPaymentStatus(String status, Pageable pageable);
    Page<Payment> findByBooking_User_UserIdAndPaymentStatus(Long userId, String status, Pageable pageable);
    
    @Query("SELECT sum(p.paymentAmount) FROM Payment p")
    Double sumOfPayments();
    
   // Double sumByPaymentAmount();
    
//    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = ?1")
    long countByPaymentStatus(String status);
    
    @Query("SELECT SUM(p.paymentAmount) FROM Payment p WHERE p.paymentStatus = 'REFUNDED'")
    Double sumRefundedPayments();  //Double sumByPaymentAmountAndPaymentStatus(String status);
    
    // Search methods
    Page<Payment> findByBooking_Venue_VenueNameContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Payment> findByBooking_User_UsernameContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Payment> findByPaymentStatusContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<Payment> findByBooking_Venue_VenueNameContainingIgnoreCaseOrBooking_User_UsernameContainingIgnoreCaseOrPaymentStatusContainingIgnoreCase(
        String venueName, String username, String status, Pageable pageable);
}