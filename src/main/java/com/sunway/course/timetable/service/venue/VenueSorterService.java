package com.sunway.course.timetable.service.venue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.venuedistance.VenueDistance;

@Service
public class VenueSorterService {

    private final VenueServiceImpl venueService;
    private final VenueDistanceServiceImpl venueDistanceService;

    public VenueSorterService(VenueServiceImpl venueService,
                               VenueDistanceServiceImpl  venueDistanceService) {
        this.venueService = venueService;
        this.venueDistanceService = venueDistanceService;
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

    public List<Venue> findNearestVenues(String fromVenueName){
        // Get all venues
        List<VenueDistance> venueDistances = venueDistanceService.getAllDistanceFromVenue(fromVenueName);

        // Map venue names to Venue objects
        Map<String, Venue> venueMap = venueService.getAllVenues().stream()
                .collect(Collectors.toMap(Venue::getName, v -> v));

        // Filter and sort by distance
        return venueDistances.stream()
                .filter(dist -> !dist.getVenueDistanceId().getVenueTo().equalsIgnoreCase(fromVenueName))
                .sorted(Comparator.comparingDouble(VenueDistance::getDistance))
                .map(dist -> venueMap.get(dist.getVenueDistanceId().getVenueTo()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

