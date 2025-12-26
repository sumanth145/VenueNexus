package com.venue.management.controller;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.entity.Venue;
import com.venue.management.service.BookingService;
import com.venue.management.service.UserService;
import com.venue.management.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listBookings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        // Admins see all, customers see theirs
        if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("EVENT_MANAGER")) {
            model.addAttribute("bookings", bookingService.getAllBookings());
        } else {
            model.addAttribute("bookings", bookingService.getCustomerBookings(user));
        }
        return "booking/list";
    }

    @GetMapping("/create/{venueId}")
    public String createBookingPage(@PathVariable Long venueId, Model model) {
        Venue venue = venueService.getVenueById(venueId).orElseThrow();
        Booking booking = new Booking();
        booking.setVenue(venue);
        model.addAttribute("booking", booking);
        return "booking/create";
    }

    @PostMapping("/create")
    public String createBooking(@RequestParam("venueId") Long venueId, @ModelAttribute Booking booking, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            Venue venue = venueService.getVenueById(venueId).orElseThrow();
            booking.setVenue(venue);
            
            User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            booking.setCustomer(user);
            
            bookingService.createBooking(booking);
            return "redirect:/bookings";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("booking", booking); 
            return "booking/create";
        }
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id) {
        bookingService.updateStatus(id, "CANCELLED");
        return "redirect:/bookings";
    }
}
