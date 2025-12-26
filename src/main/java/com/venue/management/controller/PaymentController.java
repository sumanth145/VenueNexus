package com.venue.management.controller;

import com.venue.management.entity.Booking;
import com.venue.management.entity.Payment;
import com.venue.management.service.BookingService;
import com.venue.management.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/pay/{bookingId}")
    public String paymentPage(@PathVariable Long bookingId, Model model) {
        // Need to fetch full booking object to show details
        Booking booking = bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst().orElseThrow();
                
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentAmount(booking.getVenue().getPricePerDay());
        
        model.addAttribute("payment", payment);
        return "payment/process";
    }

    @PostMapping("/process")
    public String processPayment(@RequestParam("bookingId") Long bookingId, @ModelAttribute Payment payment) {
        // Fetch booking again to ensure consistency
        Booking booking = bookingService.getAllBookings().stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst().orElseThrow();
                
        payment.setBooking(booking);
        paymentService.processPayment(payment);
        return "redirect:/bookings";
    }
}
