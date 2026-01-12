package com.venue.management.service.impl;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.repository.BookingRepository;
import com.venue.management.service.BookingService;
import com.venue.management.service.PaymentService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Booking operations.
 * Handles business logic for bookings including creation, status updates, pagination, sorting, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Service
public class BookingServiceImpl implements BookingService {

	private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PaymentService paymentService;

	/**
	 * Retrieves all bookings with optional status filter and search.
	 * 
	 * @param status Optional status filter (e.g., "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED")
	 * @param search Optional search term to search in venue name, username, or status
	 * @param pageable Pagination and sorting parameters
	 * @return Page of bookings matching the criteria
	 */
	@Override
	public Page<Booking> getAllBookings(String status, String search, Pageable pageable) {
		logger.debug("Getting all bookings with status: {}, search: {}", status, search);
		
		// If search is provided, search across multiple fields
		if (search != null && !search.trim().isEmpty()) {
			if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
				Page<Booking> searchResults = bookingRepository.findByVenue_VenueNameContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrStatusContainingIgnoreCase(
					search, search, search, pageable);
				logger.info("Found {} bookings matching search '{}' and status '{}'", 
					searchResults.getTotalElements(), search, status);
				// Filter results to match the status
				List<Booking> filteredResults = searchResults.getContent().stream()
					.filter(b -> status.equalsIgnoreCase(b.getStatus()))
					.collect(java.util.stream.Collectors.toList());
				return new org.springframework.data.domain.PageImpl<>(filteredResults, pageable, searchResults.getTotalElements());
			}
			Page<Booking> results = bookingRepository.findByVenue_VenueNameContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrStatusContainingIgnoreCase(
				search, search, search, pageable);
			logger.info("Found {} bookings matching search '{}'", results.getTotalElements(), search);
			return results;
		}
		
		// Apply status filter if provided
		if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
			Page<Booking> results = bookingRepository.findByStatus(status, pageable);
			logger.info("Found {} bookings with status '{}'", results.getTotalElements(), status);
			return results;
		}
		
		Page<Booking> results = bookingRepository.findAll(pageable);
		logger.info("Found {} total bookings", results.getTotalElements());
		return results;
	}

	/**
	 * Retrieves bookings for a specific customer with optional status filter and search.
	 * 
	 * @param user The customer user
	 * @param status Optional status filter
	 * @param search Optional search term
	 * @param pageable Pagination and sorting parameters
	 * @return Page of customer bookings matching the criteria
	 */
	@Override
	public Page<Booking> getCustomerBookings(User user, String status, String search, Pageable pageable) {
		logger.debug("Getting bookings for user: {} with status: {}, search: {}", 
			user.getUsername(), status, search);
		
		// If search is provided, filter by user first then search
		if (search != null && !search.trim().isEmpty()) {
			Page<Booking> searchResults = bookingRepository.findByUser(user, pageable);
			logger.info("Found {} bookings for user '{}' matching search '{}'", 
				searchResults.getTotalElements(), user.getUsername(), search);
			return searchResults;
		}
		
		// Apply status filter if provided
		if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
			Page<Booking> results = bookingRepository.findByUserAndStatus(user, status, pageable);
			logger.info("Found {} bookings for user '{}' with status '{}'", 
				results.getTotalElements(), user.getUsername(), status);
			return results;
		}
		
		Page<Booking> results = bookingRepository.findByUser(user, pageable);
		logger.info("Found {} bookings for user '{}'", results.getTotalElements(), user.getUsername());
		return results;
	}

	/**
	 * Creates a new booking after checking for date conflicts.
	 * 
	 * @param booking The booking to create
	 * @return The created booking
	 * @throws RuntimeException if venue is already booked for the selected date range
	 */
	@Override
	public Booking createBooking(Booking booking) {
		logger.info("Creating new booking for venue: {}, user: {}", 
			booking.getVenue().getVenueName(), booking.getUser().getUsername());
		
		// Check for date range conflicts
		List<Booking> allBookings = (List<Booking>) bookingRepository.findAll();
		boolean conflict = allBookings.stream()
				.anyMatch(b -> b.getVenue().getVenueId().equals(booking.getVenue().getVenueId())
						&& !"CANCELLED".equals(b.getStatus())
						&& isDateRangeOverlapping(booking.getEventDate(), booking.getEndDate(), b.getEventDate(),
								b.getEndDate() != null ? b.getEndDate() : b.getEventDate()));

		if (conflict) {
			logger.warn("Booking conflict detected for venue: {} on dates {} to {}", 
				booking.getVenue().getVenueName(), booking.getEventDate(), booking.getEndDate());
			throw new RuntimeException("Venue is already booked for the selected date range.");
		}
		
		booking.setStatus("PENDING");
		Booking savedBooking = bookingRepository.save(booking);
		logger.info("Booking created successfully with ID: {}", savedBooking.getBookingId());
		return savedBooking;
	}

	/**
	 * Updates the status of a booking.
	 * If status is CANCELLED, automatically triggers a refund.
	 * 
	 * @param id The booking ID
	 * @param status The new status
	 * @return The updated booking
	 */
	@Override
	public Booking updateStatus(Long id, String status) {
		logger.info("Updating booking {} status to {}", id, status);
		
		Booking booking = bookingRepository.findById(id)
			.orElseThrow(() -> {
				logger.error("Booking not found with ID: {}", id);
				return new RuntimeException("Booking not found with id: " + id);
			});
		
		booking.setStatus(status);
		
		if ("CANCELLED".equals(status)) {
			try {
				logger.info("Processing refund for cancelled booking: {}", id);
				paymentService.refundPayment(id);
			} catch (Exception e) {
				logger.error("Error refunding payment for booking {}: {}", id, e.getMessage(), e);
			}
		}
		
		Booking updatedBooking = bookingRepository.save(booking);
		logger.info("Booking {} status updated to {} successfully", id, status);
		return updatedBooking;
	}

	/**
	 * Retrieves a booking by its ID.
	 * 
	 * @param id The booking ID
	 * @return The booking
	 * @throws RuntimeException if booking is not found
	 */
	@Override
	public Booking getBookingById(Long id) {
		logger.debug("Getting booking by ID: {}", id);
		return bookingRepository.findById(id)
				.orElseThrow(() -> {
					logger.error("Booking not found with ID: {}", id);
					return new RuntimeException("Booking not found with id: " + id);
				});
	}

	/**
	 * Retrieves all bookings without pagination.
	 * 
	 * @return List of all bookings
	 */
	@Override
	public List<Booking> getTotalBookings() {
		logger.debug("Getting total bookings count");
		List<Booking> bookings = (List<Booking>) bookingRepository.findAll();
		logger.info("Retrieved {} total bookings", bookings.size());
		return bookings;
	}

	/**
	 * Checks if two date ranges overlap.
	 * 
	 * @param start1 Start date of first range
	 * @param end1 End date of first range
	 * @param start2 Start date of second range
	 * @param end2 End date of second range
	 * @return true if ranges overlap, false otherwise
	 */
	private boolean isDateRangeOverlapping(java.time.LocalDate start1, java.time.LocalDate end1,
			java.time.LocalDate start2, java.time.LocalDate end2) {
		// Standard interval overlap logic: 
		// Two ranges overlap if (StartA <= EndB) and (EndA >= StartB)
		return !start1.isAfter(end2) && !end1.isBefore(start2);
	}

}