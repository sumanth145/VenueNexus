package com.venue.management.controller;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import com.venue.management.service.SupportTicketService;
import com.venue.management.service.UserService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling support ticket-related HTTP requests.
 * Provides endpoints for creating, listing, and resolving support tickets with pagination, sorting, filtering, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Controller
@RequestMapping("/support")
public class SupportTicketController {

    private static final Logger logger = LoggerFactory.getLogger(SupportTicketController.class);

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private UserService userService;

    /**
     * Lists support tickets with pagination, sorting, filtering, and search.
     * 
     * @param userDetails The authenticated user details
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: "ticketId")
     * @param sortDir Sort direction: "asc" or "desc" (default: "desc")
     * @param status Optional status filter
     * @param search Optional search term
     * @param model The model to add attributes
     * @return The view name for support tickets list
     */
    @GetMapping
    public String listTickets(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "ticketId") String sortBy,
                             @RequestParam(defaultValue = "desc") String sortDir,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String search,
                             Model model) {
        logger.info("Listing support tickets for user: {} with page: {}, size: {}, sortBy: {}, sortDir: {}, status: {}, search: {}", 
            userDetails.getUsername(), page, size, sortBy, sortDir, status, search);
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create Sort object based on sortBy and sortDir
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<SupportTicket> ticketPage;
            if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("EVENT_MANAGER")) {
                ticketPage = supportTicketService.getAllTickets(status, search, pageable);
            } else {
                ticketPage = supportTicketService.getCustomerTickets(user, status, search, pageable);
            }
            
            model.addAttribute("tickets", ticketPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ticketPage.getTotalPages());
            model.addAttribute("totalElements", ticketPage.getTotalElements());
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentSearch", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            
            logger.info("Displayed {} support tickets (page {} of {})", 
                ticketPage.getContent().size(), page + 1, ticketPage.getTotalPages());
        } catch (Exception e) {
            logger.error("Error listing support tickets: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading support tickets: " + e.getMessage());
        }
        
        return "support/list";
    }

    /**
     * Displays the support ticket creation page.
     * 
     * @param model The model to add attributes
     * @return The view name for ticket creation
     */
    @GetMapping("/create")
    public String createTicketPage(Model model) {
        logger.debug("Displaying support ticket creation page");
        model.addAttribute("ticket", new SupportTicket());
        return "support/create";
    }

    /**
     * Creates a new support ticket.
     * 
     * @param ticket The support ticket data
     * @param userDetails The authenticated user details
     * @return Redirect to support tickets list
     */
    @PostMapping("/create")
    public String createTicket(@ModelAttribute SupportTicket ticket, 
                              @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Creating support ticket for user: {}", userDetails.getUsername());
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            ticket.setCustomer(user);
            SupportTicket createdTicket = supportTicketService.createTicket(ticket);
            logger.info("Support ticket created successfully with ID: {}", createdTicket.getTicketId());
        } catch (Exception e) {
            logger.error("Error creating support ticket: {}", e.getMessage(), e);
        }
        
        return "redirect:/support";
    }

    /**
     * Displays the support ticket resolution page.
     * 
     * @param id The support ticket ID
     * @param userDetails The authenticated user details
     * @param model The model to add attributes
     * @param redirectAttributes The redirect attributes
     * @return The view name for ticket resolution or redirect on error
     */
    @GetMapping("/resolve/{id}")
    public String resolveTicketPage(@PathVariable Long id, 
                                   @AuthenticationPrincipal UserDetails userDetails, 
                                   Model model, 
                                   RedirectAttributes redirectAttributes) {
        logger.info("Displaying resolution page for support ticket ID: {}", id);
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            SupportTicket ticket = supportTicketService.getTicketById(id);
            
            // Security check: If ticket was created by a manager, only admin can resolve it
            if (ticket.getCustomer().getRole().name().equals("EVENT_MANAGER") 
                && !user.getRole().name().equals("ADMIN")) {
                logger.warn("Unauthorized attempt to resolve ticket {} by user {}", id, user.getUsername());
                redirectAttributes.addFlashAttribute("error", "You are not authorized to resolve tickets created by managers.");
                return "redirect:/support";
            }
            
            // Check if ticket is already resolved
            if ("RESOLVED".equals(ticket.getTicketStatus())) {
                logger.warn("Attempt to resolve already resolved ticket ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "This ticket is already resolved.");
                return "redirect:/support";
            }
            
            model.addAttribute("ticket", ticket);
        } catch (Exception e) {
            logger.error("Error loading resolution page: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error loading ticket: " + e.getMessage());
            return "redirect:/support";
        }
        
        return "support/resolve";
    }

    /**
     * Resolves a support ticket with resolution notes.
     * 
     * @param id The support ticket ID
     * @param resolutionNotes The resolution notes
     * @param userDetails The authenticated user details
     * @param redirectAttributes The redirect attributes
     * @return Redirect to support tickets list
     */
    @PostMapping("/resolve/{id}")
    public String resolveTicket(@PathVariable Long id, 
                               @RequestParam("resolutionNotes") String resolutionNotes,
                               @AuthenticationPrincipal UserDetails userDetails, 
                               RedirectAttributes redirectAttributes) {
        logger.info("Resolving support ticket ID: {} by user: {}", id, userDetails.getUsername());
        
        try {
            User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            SupportTicket ticket = supportTicketService.getTicketById(id);
            
            // Security check: If ticket was created by a manager, only admin can resolve it
            if (ticket.getCustomer().getRole().name().equals("EVENT_MANAGER") 
                && !user.getRole().name().equals("ADMIN")) {
                logger.warn("Unauthorized attempt to resolve ticket {} by user {}", id, user.getUsername());
                redirectAttributes.addFlashAttribute("error", "You are not authorized to resolve tickets created by managers.");
                return "redirect:/support";
            }
            
            // Validate resolution notes
            if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
                logger.warn("Resolution notes are empty for ticket ID: {}", id);
                redirectAttributes.addFlashAttribute("error", "Resolution notes are required.");
                return "redirect:/support/resolve/" + id;
            }
            
            supportTicketService.resolveTicket(id, resolutionNotes.trim());
            redirectAttributes.addFlashAttribute("success", "Ticket resolved successfully.");
            logger.info("Support ticket {} resolved successfully", id);
        } catch (Exception e) {
            logger.error("Error resolving support ticket: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error resolving ticket: " + e.getMessage());
        }
        
        return "redirect:/support";
    }
}
