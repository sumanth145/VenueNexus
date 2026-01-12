package com.venue.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Event Venue Management System.
 * This is the entry point for the Spring Boot application.
 * 
 * @author Event Venue Management System
 * @version 1.0
 */
@SpringBootApplication
public class EventVenueManagementApplication {

	private static final Logger logger = LoggerFactory.getLogger(EventVenueManagementApplication.class);

	/**
	 * Main method to start the Spring Boot application.
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		logger.info("Starting Event Venue Management Application...");
		SpringApplication.run(EventVenueManagementApplication.class, args);
		logger.info("Event Venue Management Application started successfully!");
	}

}
