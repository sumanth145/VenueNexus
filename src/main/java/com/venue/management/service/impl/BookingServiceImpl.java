package com.venue.management.service.impl;

import com.venue.management.entity.Booking;
import com.venue.management.entity.User;
import com.venue.management.repository.BookingRepository;
import com.venue.management.service.BookingService;
import com.venue.management.service.PaymentService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookingServiceImpl implements BookingService {

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PaymentService paymentService;

	@Override
	public Page<Booking> getAllBookings(String status, Pageable pageable) {
		if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
			return bookingRepository.findByStatus(status, pageable);
		}
		return bookingRepository.findAll(pageable);
	}

	@Override
	public Page<Booking> getCustomerBookings(User user, String status, Pageable pageable) {
		if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
			return bookingRepository.findByUserAndStatus(user, status, pageable);
		}
		return bookingRepository.findByUser(user, pageable);
	}

	@Override
	public Booking createBooking(Booking booking) {
		// Check for date range conflicts
		List<Booking> allBookings = bookingRepository.findAll();
		boolean conflict = allBookings.stream()
				.anyMatch(b -> b.getVenue().getVenueId().equals(booking.getVenue().getVenueId())
						&& !"CANCELLED".equals(b.getStatus())
						&& isDateRangeOverlapping(booking.getEventDate(), booking.getEndDate(), b.getEventDate(),
								b.getEndDate() != null ? b.getEndDate() : b.getEventDate()));

		if (conflict) {
			throw new RuntimeException("Venue is already booked for the selected date range.");
		}
		booking.setStatus("PENDING");
		return bookingRepository.save(booking);
	}

	@Override
	public Booking updateStatus(Long id, String status) {
		Booking booking = bookingRepository.findById(id).orElseThrow();
		booking.setStatus(status);
		if ("CANCELLED".equals(status)) {
			try {
				paymentService.refundPayment(id);
			} catch (Exception e) {
				System.err.println("Error refunding: " + e.getMessage());
			}
		}
		return bookingRepository.save(booking);
	}

	@Override
	public Booking getBookingById(Long id) {
		return bookingRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
	}

	@Override
	public List<Booking> getTotalBookings() {

		return bookingRepository.findAll();
	}

	private boolean isDateRangeOverlapping(java.time.LocalDate start1, java.time.LocalDate end1,
			java.time.LocalDate start2, java.time.LocalDate end2) {
		// Standard interval overlap logic: 
		// Two ranges overlap if (StartA <= EndB) and (EndA >= StartB)
		return !start1.isAfter(end2) && !end1.isBefore(start2);
	}

}