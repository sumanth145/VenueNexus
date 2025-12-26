package com.venue.management.controller;

import com.venue.management.entity.Venue;
import com.venue.management.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @GetMapping
    public String listVenues(Model model) {
        model.addAttribute("venues", venueService.getAllVenues());
        return "venue/list";
    }

    @GetMapping("/add")
    public String addVenuePage(Model model) {
        model.addAttribute("venue", new Venue());
        return "venue/add";
    }

    @PostMapping("/add")
    public String addVenue(@ModelAttribute Venue venue) {
        if (venue.getVenueId() == null) {
            venue.setStatus("AVAILABLE");
        }
        venueService.saveVenue(venue);
        return "redirect:/venues";
    }

    @GetMapping("/edit/{id}")
    public String editVenuePage(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id).orElseThrow(() -> new RuntimeException("Venue not found"));
        model.addAttribute("venue", venue);
        return "venue/add"; // Reuse the add form
    }

    @GetMapping("/delete/{id}")
    public String deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return "redirect:/venues";
    }
}
