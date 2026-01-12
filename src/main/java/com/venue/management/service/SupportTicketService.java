package com.venue.management.service;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for SupportTicket operations.
 * Provides methods for managing support tickets including pagination, sorting, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
public interface SupportTicketService {
    List<SupportTicket> getAllTickets();
    List<SupportTicket> getCustomerTickets(User user);
    Page<SupportTicket> getAllTickets(String status, String search, Pageable pageable);
    Page<SupportTicket> getCustomerTickets(User user, String status, String search, Pageable pageable);
    SupportTicket createTicket(SupportTicket ticket);
    SupportTicket getTicketById(Long id);
    SupportTicket resolveTicket(Long id, String resolutionNotes);
    long countOpenTickets();
}
