package com.venue.management.controller;

import com.venue.management.entity.Venue;
import com.venue.management.service.VenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Controller for handling venue-related HTTP requests.
 * Provides endpoints for listing, creating, updating, and deleting venues with pagination, sorting, and search.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@Controller
@RequestMapping("/venues")
public class VenueController {

    private static final Logger logger = LoggerFactory.getLogger(VenueController.class);

    @Autowired
    private VenueService venueService;

    /**
     * Lists all venues with pagination, sorting, and search.
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 10)
     * @param sortBy Sort field (default: "venueId")
     * @param sortDir Sort direction: "asc" or "desc" (default: "desc")
     * @param search Optional search term to search in venue name, location, or status
     * @param model The model to add attributes
     * @return The view name for venues list
     */
    @GetMapping
    public String listVenues(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "venueId") String sortBy,
                            @RequestParam(defaultValue = "desc") String sortDir,
                            @RequestParam(required = false) String search,
                            Model model) {
        logger.info("Listing venues with page: {}, size: {}, sortBy: {}, sortDir: {}, search: {}", 
            page, size, sortBy, sortDir, search);
        
        try {
            // Create Sort object based on sortBy and sortDir
            Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Venue> venuePage = venueService.getAllVenues(search, pageable);
            
            model.addAttribute("venues", venuePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", venuePage.getTotalPages());
            model.addAttribute("totalElements", venuePage.getTotalElements());
            model.addAttribute("currentSearch", search);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            
            logger.info("Displayed {} venues (page {} of {})", 
                venuePage.getContent().size(), page + 1, venuePage.getTotalPages());
        } catch (Exception e) {
            logger.error("Error listing venues: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading venues: " + e.getMessage());
            // Fallback to non-paginated list
            model.addAttribute("venues", venueService.getAllVenues());
        }
        
        return "venue/list";
    }

    /**
     * Displays the venue creation page.
     * 
     * @param model The model to add attributes
     * @return The view name for venue creation
     */
    @GetMapping("/add")
    public String addVenuePage(Model model) {
        logger.debug("Displaying venue creation page");
        model.addAttribute("venue", new Venue());
        return "venue/add";
    }

    /**
     * Creates or updates a venue.
     * 
     * @param venue The venue data
     * @param imageFile The venue image file
     * @param redirectAttributes The redirect attributes
     * @return Redirect to venues list or venue creation page on error
     */
    @PostMapping("/add")
    public String addVenue(@ModelAttribute Venue venue,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          RedirectAttributes redirectAttributes) {
        logger.info("Saving venue: {}", venue.getVenueName());
        try {
            if (venue.getVenueId() == null) {
                logger.info("Creating new venue: {}", venue.getVenueName());
                venue.setStatus("AVAILABLE");
            } else {
                logger.info("Updating venue ID: {}, name: {}", venue.getVenueId(), venue.getVenueName());
                // When editing, preserve the existing status if not set
                if (venue.getStatus() == null || venue.getStatus().isEmpty()) {
                    Venue existingVenue = venueService.getVenueById(venue.getVenueId())
                        .orElseThrow(() -> new RuntimeException("Venue not found"));
                    venue.setStatus(existingVenue.getStatus());
                }
            }

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.debug("Saving image file: {}", imageFile.getOriginalFilename());
                String imagePath = saveImage(imageFile);
                venue.setImagePath(imagePath);
            } else if (venue.getVenueId() != null) {
                // If editing and no new image, keep existing image path
                Venue existingVenue = venueService.getVenueById(venue.getVenueId())
                    .orElseThrow(() -> new RuntimeException("Venue not found"));
                venue.setImagePath(existingVenue.getImagePath());
            }

            venueService.saveVenue(venue);
            redirectAttributes.addFlashAttribute("success", "Venue saved successfully!");
            logger.info("Venue saved successfully with ID: {}", venue.getVenueId());
            return "redirect:/venues";
        } catch (Exception e) {
            logger.error("Error saving venue: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error saving venue: " + e.getMessage());
            return "redirect:/venues/add";
        }
    }

    /**
     * Displays the venue editing page.
     * 
     * @param id The venue ID
     * @param model The model to add attributes
     * @return The view name for venue editing
     */
    @GetMapping("/edit/{id}")
    public String editVenuePage(@PathVariable Long id, Model model) {
        logger.info("Displaying edit page for venue ID: {}", id);
        
        try {
            Venue venue = venueService.getVenueById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
            model.addAttribute("venue", venue);
        } catch (Exception e) {
            logger.error("Error loading venue for editing: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading venue: " + e.getMessage());
            return "redirect:/venues";
        }
        
        return "venue/add"; // Reuse the add form
    }

    /**
     * Marks a venue as maintenance.
     * 
     * @param id The venue ID
     * @param redirectAttributes The redirect attributes
     * @return Redirect to venue edit page
     */
    @GetMapping("/maintenance/{id}")
    public String markAsMaintenance(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Marking venue {} as maintenance", id);
        
        try {
            Venue venue = venueService.getVenueById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
            venue.setStatus("MAINTENANCE");
            venueService.saveVenue(venue);
            redirectAttributes.addFlashAttribute("success", "Venue marked as maintenance successfully!");
            logger.info("Venue {} marked as maintenance successfully", id);
        } catch (Exception e) {
            logger.error("Error marking venue as maintenance: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating venue status: " + e.getMessage());
        }
        return "redirect:/venues/edit/" + id;
    }

    /**
     * Marks a venue as available.
     * 
     * @param id The venue ID
     * @param redirectAttributes The redirect attributes
     * @return Redirect to venue edit page
     */
    @GetMapping("/available/{id}")
    public String markAsAvailable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Marking venue {} as available", id);
        
        try {
            Venue venue = venueService.getVenueById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
            venue.setStatus("AVAILABLE");
            venueService.saveVenue(venue);
            redirectAttributes.addFlashAttribute("success", "Venue marked as available successfully!");
            logger.info("Venue {} marked as available successfully", id);
        } catch (Exception e) {
            logger.error("Error marking venue as available: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating venue status: " + e.getMessage());
        }
        return "redirect:/venues/edit/" + id;
    }

    /**
     * Deletes a venue and its associated image file.
     * 
     * @param id The venue ID
     * @param redirectAttributes The redirect attributes
     * @return Redirect to venues list
     */
    @PostMapping("/delete/{id}")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Deleting venue ID: {}", id);
        
        try {
            Venue venue = venueService.getVenueById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
            
            // Delete image file if exists
            if (venue.getImagePath() != null && !venue.getImagePath().isEmpty()) {
                logger.debug("Deleting image file: {}", venue.getImagePath());
                deleteImage(venue.getImagePath());
            }
            
            venueService.deleteVenue(id);
            redirectAttributes.addFlashAttribute("success", "Venue deleted successfully!");
            logger.info("Venue {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting venue: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error deleting venue: " + e.getMessage());
        }
        return "redirect:/venues";
    }

    /**
     * Saves an uploaded image file.
     * 
     * @param file The image file to save
     * @return The relative path of the saved image
     * @throws IOException if an I/O error occurs
     */
    private String saveImage(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get("src/main/resources/static/images");
        if (!Files.exists(uploadPath)) {
            logger.debug("Creating upload directory: {}", uploadPath);
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("Image saved successfully: {}", filename);

        // Return relative path for database storage
        return "/images/" + filename;
    }

    /**
     * Deletes an image file.
     * 
     * @param imagePath The relative path of the image to delete
     */
    private void deleteImage(String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Path filePath = Paths.get("src/main/resources/static" + imagePath);
                Files.deleteIfExists(filePath);
                logger.debug("Image deleted successfully: {}", imagePath);
            }
        } catch (IOException e) {
            // Log error but don't fail the delete operation
            logger.error("Error deleting image file {}: {}", imagePath, e.getMessage(), e);
        }
    }
}
