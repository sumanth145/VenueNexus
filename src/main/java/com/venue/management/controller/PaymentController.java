package com.venue.management.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import com.venue.management.entity.User;
import com.venue.management.service.BookingService;
import com.venue.management.service.PaymentService;
import com.venue.management.service.UserService;

/**
 * Controller for handling payment-related HTTP requests.
 * Provides endpoints for processing payments, listing payments with pagination, sorting, filtering, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Controller
@RequestMapping("/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserService userService;
    
    @Autowired
    private BookingService bookingService;
    
    /**
     * Displays the payment page for a specific booking.
     * 
     * @param bookingId The booking ID
     * @param model The model to add attributes
     * @return The view name for payment processing
     */
    @GetMapping("/pay/{bookingId}")
    public String paymentPage(@PathVariable Long bookingId, Model model) {
        logger.info("Displaying payment page for booking ID: {}", bookingId);
        
        try {
            Booking booking = bookingService.getBookingById(bookingId);
                    
            Payment payment = new Payment();
            payment.setBooking(booking);
            
            // Calculate total amount based on number of days
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                booking.getEventDate(), 
                booking.getEndDate()
            ) + 1; // +1 to include both start and end dates
            
            double totalAmount = booking.getVenue().getPricePerDay() * days;
            payment.setPaymentAmount(totalAmount);
            
            logger.debug("Calculated payment amount: {} for {} days", totalAmount, days);
            
            model.addAttribute("payment", payment);
        } catch (Exception e) {
            logger.error("Error loading payment page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading booking: " + e.getMessage());
            return "redirect:/bookings";
        }
        
        return "payment/process";
    }

    /**
     * Processes a payment for a booking.
     * 
     * @param bookingId The booking ID
     * @param payment The payment data
     * @return Redirect to bookings list
     */
    @PostMapping("/process")
    public String processPayment(@RequestParam("bookingId") Long bookingId, 
                                @ModelAttribute Payment payment) {
        logger.info("Processing payment for booking ID: {}", bookingId);
        
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            payment.setBooking(booking);
            paymentService.processPayment(payment);
            logger.info("Payment processed successfully for booking ID: {}", bookingId);
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
        }
        
        return "redirect:/bookings";
    }
    
    /**
     * Lists payments for the authenticated user with pagination, sorting, filtering, and search.
     * 
     * @param userDetails The authenticated user details
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: "paymentId")
     * @param sortDir Sort direction: "asc" or "desc" (default: "desc")
     * @param status Optional status filter
     * @param search Optional search term
     * @param model The model to add attributes
     * @return The view name for user payments
     */
    @GetMapping("/my-payments")
    public String myPayments(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "paymentId") String sortBy,
                             @RequestParam(defaultValue = "desc") String sortDir,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String search,
                             Model model) {
        logger.info("Listing payments for user: {} with page: {}, size: {}, sortBy: {}, sortDir: {}, status: {}, search: {}", 
            userDetails.getUsername(), page, size, sortBy, sortDir, status, search);
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create Sort object based on sortBy and sortDir
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Payment> paymentPage = paymentService.getUserPayments(user, status, search, pageable);
            
            model.addAttribute("payments", paymentPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", paymentPage.getTotalPages());
            model.addAttribute("totalElements", paymentPage.getTotalElements());
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSearch", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            
            logger.info("Displayed {} payments for user (page {} of {})", 
                paymentPage.getContent().size(), page + 1, paymentPage.getTotalPages());
        } catch (Exception e) {
            logger.error("Error listing user payments: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading payments: " + e.getMessage());
        }
        
        return "payment/my-payments";
    }

    /**
     * Lists all payments for admin with pagination, sorting, filtering, and search.
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: "paymentId")
     * @param sortDir Sort direction: "asc" or "desc" (default: "desc")
     * @param status Optional status filter
     * @param search Optional search term
     * @param model The model to add attributes
     * @return The view name for admin payments
     */
    @GetMapping("/admin")
    public String adminPayments(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "paymentId") String sortBy,
                                @RequestParam(defaultValue = "desc") String sortDir,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String search,
                                Model model) {
        logger.info("Listing admin payments with page: {}, size: {}, sortBy: {}, sortDir: {}, status: {}, search: {}", 
            page, size, sortBy, sortDir, status, search);
        
        try {
            // Create Sort object based on sortBy and sortDir
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Payment> paymentPage = paymentService.getAllPayments(status, search, pageable);
            
            model.addAttribute("allPayments", paymentPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", paymentPage.getTotalPages());
            model.addAttribute("totalElements", paymentPage.getTotalElements());
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSearch", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            
            // Load Statistics
            model.addAttribute("totalEarnings", paymentService.getTotalEarnings());
            model.addAttribute("successfulPaymentsCount", paymentService.getSuccessfulPaymentsCount());
            model.addAttribute("pendingPaymentsCount", paymentService.getPendingPaymentsCount());
            model.addAttribute("refundedPaymentsCount", paymentService.getRefundedPaymentsCount());
            model.addAttribute("totalRefundedAmount", paymentService.getTotalRefundedAmount());
            
            logger.info("Displayed {} admin payments (page {} of {})", 
                paymentPage.getContent().size(), page + 1, paymentPage.getTotalPages());
        } catch (Exception e) {
            logger.error("Error listing admin payments: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading payments: " + e.getMessage());
        }
        
        return "payment/admin-payments";
    }
}
















//package com.venue.management.controller;
//
//import com.venue.management.entity.Booking;
//import com.venue.management.entity.Payment;
//import com.venue.management.entity.User;
//import com.venue.management.service.BookingService;
//import com.venue.management.service.PaymentService;
//import com.venue.management.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/payments")
//public class PaymentController {
//
//    @Autowired
//    private PaymentService paymentService;
//
//    @Autowired
//    private BookingService bookingService;
//
//    @Autowired
//    private UserService userService;
//
//    @GetMapping("/pay/{bookingId}")
//    public String paymentPage(@PathVariable Long bookingId, Model model) {
//        // Need to fetch full booking object to show details
//        Booking booking = bookingService.getAllBookings().stream()
//                .filter(b -> b.getBookingId().equals(bookingId))
//                .findFirst().orElseThrow();
//                
//        Payment payment = new Payment();
//        payment.setBooking(booking);
//        
//        // Calculate total amount based on number of days
//        long days = java.time.temporal.ChronoUnit.DAYS.between(
//            booking.getEventDate(), 
//            booking.getEndDate()
//        ) + 1; // +1 to include both start and end dates
//        double totalAmount = booking.getVenue().getPricePerDay() * days;
//        payment.setPaymentAmount(totalAmount);
//        
//        model.addAttribute("payment", payment);
//        return "payment/process";
//    }
//
//    @PostMapping("/process")
//    public String processPayment(@RequestParam("bookingId") Long bookingId, @ModelAttribute Payment payment) {
//        // Fetch booking again to ensure consistency
//        Booking booking = bookingService.getAllBookings().stream()
//                .filter(b -> b.getBookingId().equals(bookingId))
//                .findFirst().orElseThrow();
//                
//        payment.setBooking(booking);
//        paymentService.processPayment(payment);
//        return "redirect:/bookings";
//    }
//
//    @GetMapping("/my-payments")
//    public String myPayments(@AuthenticationPrincipal UserDetails userDetails, Model model) {
//        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
//        List<Payment> payments = paymentService.getUserPayments(user);
//        
//        // For each payment, determine the display status based on booking status
//        for (Payment payment : payments) {
//            if ("CANCELLED".equals(payment.getBooking().getStatus())) {
//                // If booking is cancelled, ensure payment shows as refunded
//                if (!"REFUNDED".equals(payment.getPaymentStatus())) {
//                    payment.setPaymentStatus("REFUNDED");
//                }
//            }
//        }
//        
//        // Calculate summary statistics
//        double totalPaid = payments.stream()
//            .filter(p -> "SUCCESS".equals(p.getPaymentStatus()) && !"CANCELLED".equals(p.getBooking().getStatus()))
//            .mapToDouble(Payment::getPaymentAmount)
//            .sum();
//        
//        long successfulCount = payments.stream()
//            .filter(p -> "SUCCESS".equals(p.getPaymentStatus()) && !"CANCELLED".equals(p.getBooking().getStatus()))
//            .count();
//        
//        double refundedAmount = payments.stream()
//            .filter(p -> "CANCELLED".equals(p.getBooking().getStatus()))
//            .mapToDouble(Payment::getPaymentAmount)
//            .sum();
//        
//        model.addAttribute("payments", payments);
//        model.addAttribute("totalPaid", totalPaid);
//        model.addAttribute("successfulCount", successfulCount);
//        model.addAttribute("refundedAmount", refundedAmount);
//        return "payment/my-payments";
//    }
//
//    @GetMapping("/admin")
//    public String adminPayments(Model model) {
//        model.addAttribute("totalEarnings", paymentService.getTotalEarnings());
//        model.addAttribute("successfulPaymentsCount", paymentService.getSuccessfulPaymentsCount());
//        model.addAttribute("pendingPaymentsCount", paymentService.getPendingPaymentsCount());
//        model.addAttribute("refundedPaymentsCount", paymentService.getRefundedPaymentsCount());
//        model.addAttribute("totalRefundedAmount", paymentService.getTotalRefundedAmount());
//        model.addAttribute("allPayments", paymentService.getAllPayments());
//        return "payment/admin-payments";
//    }
//}
