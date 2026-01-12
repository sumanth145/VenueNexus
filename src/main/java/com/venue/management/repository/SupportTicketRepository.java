package com.venue.management.repository;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for SupportTicket entity operations.
 * Extends both CrudRepository and PagingAndSortingRepository to provide CRUD and pagination/sorting capabilities.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Repository
public interface SupportTicketRepository extends CrudRepository<SupportTicket, Long>, PagingAndSortingRepository<SupportTicket, Long> {
    List<SupportTicket> findByCustomer(User customer);
    long countByTicketStatus(String ticketStatus);
    
    List<SupportTicket> findByIssueType(String issueType);
    
    // Pagination and sorting methods
    Page<SupportTicket> findByCustomer(User customer, Pageable pageable);
    Page<SupportTicket> findByTicketStatus(String status, Pageable pageable);
    Page<SupportTicket> findByIssueType(String issueType, Pageable pageable);
    Page<SupportTicket> findByCustomerAndTicketStatus(User customer, String status, Pageable pageable);
    
    // Search methods
    Page<SupportTicket> findByCustomer_UsernameContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<SupportTicket> findByIssueTypeContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<SupportTicket> findByTicketStatusContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<SupportTicket> findByIssueDescriptionContainingIgnoreCase(String searchTerm, Pageable pageable);
    Page<SupportTicket> findByCustomer_UsernameContainingIgnoreCaseOrIssueTypeContainingIgnoreCaseOrTicketStatusContainingIgnoreCaseOrIssueDescriptionContainingIgnoreCase(
        String username, String issueType, String status, String description, Pageable pageable);
}
