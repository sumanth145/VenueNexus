package com.venue.management.controller;

import com.venue.management.entity.SupportTicket;
import com.venue.management.entity.User;
import com.venue.management.service.SupportTicketService;
import com.venue.management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support")
public class SupportTicketController {

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listTickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        
        if (user.getRole().name().equals("ADMIN")) {
            model.addAttribute("tickets", supportTicketService.getAllTickets());
        } else {
            model.addAttribute("tickets", supportTicketService.getCustomerTickets(user));
        }
        return "support/list";
    }

    @GetMapping("/create")
    public String createTicketPage(Model model) {
        model.addAttribute("ticket", new SupportTicket());
        return "support/create";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute SupportTicket ticket, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        ticket.setCustomer(user);
        supportTicketService.createTicket(ticket);
        return "redirect:/support";
    }

    @GetMapping("/resolve/{id}")
    public String resolveTicket(@PathVariable Long id) {
        supportTicketService.resolveTicket(id);
        return "redirect:/support";
    }
}
