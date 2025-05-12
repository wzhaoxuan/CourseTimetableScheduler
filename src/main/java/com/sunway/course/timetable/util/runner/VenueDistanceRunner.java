package com.sunway.course.timetable.util.runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sunway.course.timetable.service.generator.VenueDistanceGenerator;

@Configuration
public class VenueDistanceRunner {
    Logger logger = LoggerFactory.getLogger(VenueDistanceRunner.class);


    @Bean
    public CommandLineRunner generateVenueDistances(VenueDistanceGenerator venueDistanceGenerator) {
        return args -> {
            logger.info(">>> TESTING LOGGER OUTPUT <<<");
            // Generate venue distances
            venueDistanceGenerator.generateVenueDistances();
            System.out.println("Venue distances saved into database.");
        };
    }
}
