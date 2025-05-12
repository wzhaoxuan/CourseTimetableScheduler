package com.sunway.course.timetable.service.generator;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;
import com.sunway.course.timetable.repository.VenueDistanceRepository;
import com.sunway.course.timetable.repository.VenueRepository;

@Service
public class VenueDistanceGenerator {

    private static Logger logger = LoggerFactory.getLogger(VenueDistanceGenerator.class);

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

        String fromFloor = from.getFloor();
        String toFloor = to.getFloor();
        String fromFloorType = from.getFloorType();
        String toFloorType = to.getFloorType();

        int fromLevel = extractFloorLevel(fromFloor);
        int toLevel = extractFloorLevel(toFloor);
        int levelDifference = Math.abs(fromLevel - toLevel);

        boolean isUniversityFrom = fromFloorType.toLowerCase().contains("university");
        boolean isUniversityTo = toFloorType.toLowerCase().contains("university");
        boolean isCollegeFrom = fromFloorType.toLowerCase().contains("college");
        boolean isCollegeTo = toFloorType.toLowerCase().contains("college");

        boolean sameLevel = fromLevel == toLevel;
        boolean sameFloorType = fromFloorType.equalsIgnoreCase(toFloorType);

        // Rule 1: University → University, same level, same floor types
        if (isUniversityFrom && isUniversityTo && sameLevel && sameFloorType) return 20.0;

        // Rule 2: University → University, same level, going between west/center/east
        if (isUniversityFrom && isUniversityTo && sameLevel && !sameFloorType) return 50.0;

        // Rule 3: University → University, different level, going between west/center/east
        if (isUniversityFrom && isUniversityTo && !sameLevel && !sameFloorType) return 50.0 * levelDifference;

        // Rule 4: From university → college
        if (isUniversityFrom && isCollegeTo) {
            int adjustedFromLevel = Math.abs(fromLevel - 3);
            int adjustedToLevel = Math.abs(toLevel - 3);
            if(fromLevel == 3){
                return 150.0 * adjustedToLevel;
            }
            return 150.0 * adjustedFromLevel * adjustedToLevel;
        }

        // Rule 4 reversed: From college → university
        if (isCollegeFrom && isUniversityTo) {
            int adjustedFromLevel = Math.abs(fromLevel - 3);
            int adjustedToLevel = Math.abs(toLevel - 3);
            if(toLevel == 3){
                return 150.0 * adjustedFromLevel;
            }
            return 150.0 * adjustedToLevel * adjustedFromLevel;
        }

        // Rule 5: College → College, South West ↔ South East, different levels
        boolean isSW = fromFloorType.toLowerCase().contains("south west") || toFloorType.toLowerCase().contains("south west");
        boolean isSE = fromFloorType.toLowerCase().contains("south east") || toFloorType.toLowerCase().contains("south east");

        if (isCollegeFrom && isCollegeTo && isSW && isSE && !sameLevel) return 50.0 * levelDifference;

        // Default: University → University, different level, same floor types
        return 50.0 * levelDifference;
    }

    private int extractFloorLevel(String floorLevel) {
        if(floorLevel == null || floorLevel.isEmpty()) return 0;
        if(floorLevel.toLowerCase().contains("ground")) return 0;

        try{
            return Integer.parseInt(floorLevel.replaceAll("[^0-9]", ""));
        }catch (NumberFormatException e) {
            // Handle the case where floorType does not contain a valid number
            return 0; // Default to ground level if parsing fails
        }
    }
}
