package com.sunway.course.timetable.service.venue;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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

        return Collections.unmodifiableList(venues);
    }

    public List<Venue> findNearestVenues(String fromVenueName){
        // Get all venues
        List<VenueDistance> venueDistances = venueDistanceService.getAllDistanceFromVenue(fromVenueName);

        // Sort by distance
        venueDistances.sort(Comparator.comparingDouble(VenueDistance::getDistance));

        // Convert to Venue objects
        return venueDistances.stream()
            .map(d -> venueService.getVenueByName(d.getVenueDistanceId().getVenueTo()).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}

