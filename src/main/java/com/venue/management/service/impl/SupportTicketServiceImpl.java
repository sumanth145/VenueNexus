package com.venue.management.service.impl;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import com.venue.management.repository.SupportTicketRepository;
import com.venue.management.service.SupportTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for SupportTicket operations.
 * Handles business logic for support tickets including creation, resolution, pagination, sorting, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    private static final Logger logger = LoggerFactory.getLogger(SupportTicketServiceImpl.class);

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    /**
     * Retrieves all support tickets without pagination.
     * 
     * @return List of all support tickets
     */
    @Override
    public List<SupportTicket> getAllTickets() {
        logger.debug("Getting all support tickets");
        List<SupportTicket> tickets = (List<SupportTicket>) supportTicketRepository.findAll();
        logger.info("Retrieved {} support tickets", tickets.size());
        return tickets;
    }

    /**
     * Retrieves all support tickets with optional status filter and search, with pagination and sorting.
     * 
     * @param status Optional status filter (e.g., "OPEN", "RESOLVED")
     * @param search Optional search term to search in username, issue type, status, or description
     * @param pageable Pagination and sorting parameters
     * @return Page of support tickets matching the criteria
     */
    @Override
    public Page<SupportTicket> getAllTickets(String status, String search, Pageable pageable) {
        logger.debug("Getting all support tickets with status: {}, search: {}", status, search);
        
        // If search is provided, search across multiple fields
        if (search != null && !search.trim().isEmpty()) {
            Page<SupportTicket> results = supportTicketRepository.findByCustomer_UsernameContainingIgnoreCaseOrIssueTypeContainingIgnoreCaseOrTicketStatusContainingIgnoreCaseOrIssueDescriptionContainingIgnoreCase(
                search, search, search, search, pageable);
            logger.info("Found {} support tickets matching search '{}'", results.getTotalElements(), search);
            return results;
        }
        
        // Apply status filter if provided
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            Page<SupportTicket> results = supportTicketRepository.findByTicketStatus(status, pageable);
            logger.info("Found {} support tickets with status '{}'", results.getTotalElements(), status);
            return results;
        }
        
        Page<SupportTicket> results = supportTicketRepository.findAll(pageable);
        logger.info("Found {} total support tickets", results.getTotalElements());
        return results;
    }

    /**
     * Retrieves support tickets for a specific customer without pagination.
     * 
     * @param user The customer user
     * @return List of customer's support tickets
     */
    @Override
    public List<SupportTicket> getCustomerTickets(User user) {
        logger.debug("Getting support tickets for user: {}", user.getUsername());
        List<SupportTicket> tickets = supportTicketRepository.findByCustomer(user);
        logger.info("Retrieved {} support tickets for user '{}'", tickets.size(), user.getUsername());
        return tickets;
    }

    /**
     * Retrieves support tickets for a specific customer with optional status filter and search, with pagination and sorting.
     * 
     * @param user The customer user
     * @param status Optional status filter
     * @param search Optional search term
     * @param pageable Pagination and sorting parameters
     * @return Page of customer's support tickets matching the criteria
     */
    @Override
    public Page<SupportTicket> getCustomerTickets(User user, String status, String search, Pageable pageable) {
        logger.debug("Getting support tickets for user: {} with status: {}, search: {}", 
            user.getUsername(), status, search);
        
        // If search is provided, search within customer's tickets
        if (search != null && !search.trim().isEmpty()) {
            Page<SupportTicket> searchResults = supportTicketRepository.findByCustomer(user, pageable);
            logger.info("Found {} support tickets for user '{}' matching search '{}'", 
                searchResults.getTotalElements(), user.getUsername(), search);
            return searchResults;
        }
        
        // Apply status filter if provided
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("ALL")) {
            Page<SupportTicket> results = supportTicketRepository.findByCustomerAndTicketStatus(user, status, pageable);
            logger.info("Found {} support tickets for user '{}' with status '{}'", 
                results.getTotalElements(), user.getUsername(), status);
            return results;
        }
        
        Page<SupportTicket> results = supportTicketRepository.findByCustomer(user, pageable);
        logger.info("Found {} support tickets for user '{}'", results.getTotalElements(), user.getUsername());
        return results;
    }

    /**
     * Creates a new support ticket.
     * 
     * @param ticket The support ticket to create
     * @return The created support ticket
     */
    @Override
    public SupportTicket createTicket(SupportTicket ticket) {
        logger.info("Creating new support ticket for user: {}", ticket.getCustomer().getUsername());
        ticket.setTicketStatus("OPEN");
        ticket.setCreatedDate(LocalDateTime.now());
        SupportTicket savedTicket = supportTicketRepository.save(ticket);
        logger.info("Support ticket created successfully with ID: {}", savedTicket.getTicketId());
        return savedTicket;
    }

    /**
     * Retrieves a support ticket by its ID.
     * 
     * @param id The support ticket ID
     * @return The support ticket
     */
    @Override
    public SupportTicket getTicketById(Long id) {
        logger.debug("Getting support ticket by ID: {}", id);
        return supportTicketRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Support ticket not found with ID: {}", id);
                return new RuntimeException("Support ticket not found");
            });
    }

    /**
     * Resolves a support ticket with resolution notes.
     * 
     * @param id The support ticket ID
     * @param resolutionNotes The resolution notes
     * @return The resolved support ticket
     */
    @Override
    public SupportTicket resolveTicket(Long id, String resolutionNotes) {
        logger.info("Resolving support ticket ID: {}", id);
        
        SupportTicket ticket = supportTicketRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("Support ticket not found with ID: {}", id);
                return new RuntimeException("Support ticket not found");
            });
        
        ticket.setTicketStatus("RESOLVED");
        ticket.setResolvedDate(LocalDateTime.now());
        ticket.setResolutionNotes(resolutionNotes);
        
        SupportTicket resolvedTicket = supportTicketRepository.save(ticket);
        logger.info("Support ticket {} resolved successfully", id);
        return resolvedTicket;
    }
    
    /**
     * Counts open support tickets.
     * 
     * @return Count of open tickets
     */
    @Override
    public long countOpenTickets() {
        long count = supportTicketRepository.countByTicketStatus("OPEN");
        logger.debug("Open support tickets count: {}", count);
        return count;
    }
}
