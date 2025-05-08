package com.sunway.course.timetable.service.generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.repository.VenueDistanceRepository;
import com.sunway.course.timetable.repository.VenueRepository;


@Service
public class VenueDistanceGenerator {

    private static final Logger logger = LoggerFactory.getLogger(VenueDistanceGenerator.class);

    private final VenueRepository venueRepository;
    private final VenueDistanceRepository venueDistanceRepository;

    public VenueDistanceGenerator(VenueRepository venueRepository, VenueDistanceRepository venueDistanceRepository) {
        this.venueRepository = venueRepository;
        this.venueDistanceRepository = venueDistanceRepository;
    }

    public void generateVenueDistances(){
        List<Venue> venues = venueRepository.findAll();
        List<VenueDistance> venueDistances = new ArrayList<>();
        

        for(Venue from: venues) {
            for(Venue to: venues) {
                String venueFrom = from.getName();
                String venueTo = to.getName();

                VenueDistanceId id = new VenueDistanceId(from.getId(), venueFrom, venueTo);

                if(venueDistanceRepository.existsById(id)){
                    continue; // Skip if already exists
                }

                double distance = calculateDistance(from, to); // Replace with actual distance calculation logic
                VenueDistance distanceEntry = new VenueDistance(id, distance);
                distanceEntry.setVenue(from); // Set the venue for the distance entry
                venueDistances.add(distanceEntry);
            }
        }

        venueDistanceRepository.saveAll(venueDistances);
    }

    private double calculateDistance(Venue from, Venue to) {
        // Placeholder for actual distance calculation logic
        // For example, you could use Haversine formula or any other method to calculate distance between two venues
        if (from.getName().equals(to.getName())) return 0.0; // Same venue distance is 0

        String fromFloorType = from.getFloorType();
        String toFloorType = to.getFloorType();
        int fromFloorLevel = extractFloorLevel(fromFloorType);
        int toFloorLevel = extractFloorLevel(toFloorType);
        int floorDifference = Math.abs(fromFloorLevel - toFloorLevel);

        boolean sameType = fromFloorType.equals(toFloorType);
        boolean isCollege = fromFloorType.contains("College") || toFloorType.contains("College");

        if(sameType && fromFloorLevel == toFloorLevel) return 10.0;
        if(sameType) return 15.0 * floorDifference;
        if(!sameType || !isCollege) return 100 + 20.0 * floorDifference;
        return 150 + 20 * floorDifference;
    }

    private int extractFloorLevel(String floorType) {
        if(floorType == null || floorType.isEmpty()) return 0;
        if(floorType.toLowerCase().contains("ground")) return 0;

        try{
            return Integer.parseInt(floorType.replaceAll("[^0-9]", ""));
        }catch (NumberFormatException e) {
            // Handle the case where floorType does not contain a valid number
            return 0; // Default to ground level if parsing fails
        }
    }
}
