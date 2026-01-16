package com.venue.management.controller;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.entity.Venue;
import com.venue.management.service.BookingService;
import com.venue.management.service.UserService;
import com.venue.management.service.VenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.LocalDate;
/**
 * Controller for handling booking-related HTTP requests.
 * Provides endpoints for listing, creating, and managing bookings with pagination, sorting, filtering, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Controller
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private UserService userService;

    /**
     * Lists all bookings with pagination, sorting, filtering, and search.
     * 
     * @param userDetails The authenticated user details
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: "bookingId")
     * @param sortDir Sort direction: "asc" or "desc" (default: "desc")
     * @param status Optional status filter
     * @param search Optional search term
     * @param model The model to add attributes
     * @return The view name for bookings list
     */
    @GetMapping
    public String listBookings(@AuthenticationPrincipal UserDetails userDetails, 
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(defaultValue = "bookingId") String sortBy,
                               @RequestParam(defaultValue = "desc") String sortDir,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String search,
                               Model model) {
        logger.info("Listing bookings for user: {} with page: {}, size: {}, sortBy: {}, sortDir: {}, status: {}, search: {}", 
            userDetails.getUsername(), page, size, sortBy, sortDir, status, search);
        
        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            
            // Create Sort object based on sortBy and sortDir
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending()  // sort by {column} ascending order
                : Sort.by(sortBy).descending(); // sort by {column} descending order
            
            // Define paging and sorting
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Auto-mark bookings as COMPLETED if current date passes end date
            List<Booking> allBookings = bookingService.getTotalBookings();
            
            LocalDate currentDate = LocalDate.now();
            for (Booking booking : allBookings) {
                if (!"COMPLETED".equals(booking.getStatus()) && 
                    !"CANCELLED".equals(booking.getStatus()) && 
                    booking.getEndDate() != null &&
                    currentDate.isAfter(booking.getEndDate())) {
                    logger.debug("Auto-marking booking {} as COMPLETED", booking.getBookingId());
                    bookingService.updateStatus(booking.getBookingId(), "COMPLETED");
                }
            }
            
            
            // Admins and managers see all, customers see theirs
            Page<Booking> bookingPage;
            if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("EVENT_MANAGER")) {
                bookingPage = bookingService.getAllBookings(status, search, pageable);
            } else {
                bookingPage = bookingService.getCustomerBookings(user, status, search, pageable);
            }

            model.addAttribute("bookings", bookingPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookingPage.getTotalPages());
            model.addAttribute("totalElements", bookingPage.getTotalElements());
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSearch", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            
            logger.info("Displayed {} bookings (page {} of {})", 
                bookingPage.getContent().size(), page + 1, bookingPage.getTotalPages());
        } catch (Exception e) {
            logger.error("Error listing bookings: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading bookings: " + e.getMessage());
        }
        
        return "booking/list";
    }

    /**
     * Displays the booking creation page for a specific venue.
     * 
     * @param venueId The venue ID
     * @param model The model to add attributes
     * @return The view name for booking creation
     */
    @GetMapping("/create/{venueId}")
    public String createBookingPage(@PathVariable Long venueId, Model model) {
        logger.info("Displaying booking creation page for venue ID: {}", venueId);
        
        try {
            Venue venue = venueService.getVenueById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
            Booking booking = new Booking();
            booking.setVenue(venue);
            model.addAttribute("booking", booking);
        } catch (Exception e) {
            logger.error("Error loading booking creation page: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading venue: " + e.getMessage());
            return "redirect:/venues";
        }
        
        return "booking/create";
    }

    /**
     * Creates a new booking.
     * 
     * @param venueId The venue ID
     * @param booking The booking data
     * @param userDetails The authenticated user details
     * @param model The model to add attributes
     * @return Redirect to bookings list or booking creation page on error
     */
    @PostMapping("/create")
    public String createBooking(@RequestParam("venueId") Long venueId, 
                               @ModelAttribute Booking booking, 
                               @AuthenticationPrincipal UserDetails userDetails, 
                               Model model) {
        logger.info("Creating booking for venue ID: {} by user: {}", 
            venueId, userDetails.getUsername());
        
        try {
            Venue venue = venueService.getVenueById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
            booking.setVenue(venue);
            
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            booking.setUser(user);
            
            // Set start date to current date if not provided
            if (booking.getEventDate() == null) {
                booking.setEventDate(LocalDate.now());
            }
            
            // Validate end date is after start date
            if (booking.getEndDate() == null || booking.getEndDate().isBefore(booking.getEventDate())) {
                booking.setEndDate(booking.getEventDate());
            }
            
            bookingService.createBooking(booking);
            logger.info("Booking created successfully with ID: {}", booking.getBookingId());
            return "redirect:/bookings";
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("booking", booking);
            try {
                model.addAttribute("venue", venueService.getVenueById(venueId).orElseThrow());
            } catch (Exception ex) {
                logger.error("Error loading venue: {}", ex.getMessage());
            }
            return "booking/create";
        }
    }

    /**
     * Cancels a booking.
     * 
     * @param id The booking ID
     * @return Redirect to bookings list
     */
    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id) {
        logger.info("Cancelling booking ID: {}", id);
        
        try {
            bookingService.updateStatus(id, "CANCELLED");
            logger.info("Booking {} cancelled successfully", id);
        } catch (Exception e) {
            logger.error("Error cancelling booking {}: {}", id, e.getMessage(), e);
        }
        
        return "redirect:/bookings";
    }
  
    /**
     * Marks a booking as completed.
     * 
     * @param id The booking ID
     * @return Redirect to bookings list
     */
    @GetMapping("/complete/{id}")
    public String completeBooking(@PathVariable Long id) {
        logger.info("Completing booking ID: {}", id);
        
        try {
            bookingService.updateStatus(id, "COMPLETED");
            logger.info("Booking {} completed successfully", id);
        } catch (Exception e) {
            logger.error("Error completing booking {}: {}", id, e.getMessage(), e);
        }
        
        return "redirect:/bookings";
    }
}
