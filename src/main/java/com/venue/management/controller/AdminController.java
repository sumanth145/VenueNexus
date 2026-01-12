package com.venue.management.controller;

import com.venue.management.entity.Role;
import com.venue.management.entity.User;
import com.venue.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/approvals")
    public String viewPendingApprovals(Model model) {
        // Fetch all users who are EVENT_MANAGER and NOT enabled
        List<User> allUsers = (List<User>) userRepository.findAll();
        List<User> pendingManagers = allUsers.stream()
                .filter(u -> u.getRole() == Role.EVENT_MANAGER && !u.isEnabled())
                .toList();
        
     // Example addition to AdminController.java
        List<User> approvedManagers = allUsers 
            .stream()
            .filter(u -> u.getRole() == Role.EVENT_MANAGER && u.isEnabled())
            .collect(Collectors.toList());
        
        model.addAttribute("approvedManagers", approvedManagers);
        
        model.addAttribute("pendingManagers", pendingManagers);
        return "admin/approvals";
    }

    @PostMapping("/approve/{userId}")
    public String approveManager(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() == Role.EVENT_MANAGER) {
            user.setEnabled(true);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Event Manager approved successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found or invalid role.");
        }
        return "redirect:/admin/approvals";
    }

    @PostMapping("/reject/{userId}")
    public String rejectManager(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRole() == Role.EVENT_MANAGER) {
            // Delete the user request
            userRepository.delete(user);
            
            redirectAttributes.addFlashAttribute("success", "Request rejected and removed.");
        }
        return "redirect:/admin/approvals";
    }
}
