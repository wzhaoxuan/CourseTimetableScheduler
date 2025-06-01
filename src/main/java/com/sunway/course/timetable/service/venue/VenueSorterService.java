package com.sunway.course.timetable.service.venue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Venue;

@Service
public class VenueSorterService {

    private final VenueServiceImpl venueService;

    public VenueSorterService(VenueServiceImpl venueService) {
        this.venueService = venueService;
    }

    /**
     * Sorts venues by capacity in ascending order.
     * 
     * @param venues List of Venue entities to sort.
     * @return A new sorted list (lowest to highest capacity).
     */
    public List<Venue> sortByAscendingCapacity() {
        List<Venue> venues = venueService.getAllVenues();
        // Separate venues by capacity criteria
        List<Venue> smallVenues = new ArrayList<>();
        List<Venue> largeVenues = new ArrayList<>();

        for (Venue venue : venues) {
            if (venue.getCapacity() == 35) {
                smallVenues.add(venue);
            } else if (venue.getCapacity() >= 120) {
                largeVenues.add(venue);
            }
        }

        // Sort each list ascending by capacity (though all smallVenues are 35)
        smallVenues.sort(Comparator.comparingInt(Venue::getCapacity));
        largeVenues.sort(Comparator.comparingInt(Venue::getCapacity));

        // Take first 5 venues from each list (or fewer if not enough)
        List<Venue> result = new ArrayList<>();
        result.addAll(smallVenues.subList(0, Math.min(9, smallVenues.size())));
        result.addAll(largeVenues.subList(0, Math.min(3, largeVenues.size())));

        return Collections.unmodifiableList(result);
    }
}

