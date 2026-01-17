package com.venue.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventVenueManagementApplication {

	private static final Logger logger = LoggerFactory.getLogger(EventVenueManagementApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Event Venue Management Application...");
		SpringApplication.run(EventVenueManagementApplication.class, args);
		logger.info("Event Venue Management Application started successfully!");
	}

}
