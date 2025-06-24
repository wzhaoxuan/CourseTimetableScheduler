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
import com.sunway.course.timetable.util.VenueDistanceUtils;

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
                String venueFrom = VenueDistanceUtils.sanitizeVenueName(from.getName());
                String venueTo = VenueDistanceUtils.sanitizeVenueName(to.getName());

                VenueDistanceId id = new VenueDistanceId(venueFrom, venueTo);

                double distance =  VenueDistanceUtils.calculateDistance(from, to); // Replace with actual distance calculation logic
                VenueDistance distanceEntry = new VenueDistance(id, distance);
                distanceEntry.setVenue(from); // Set the venue for the distance entry
                venueDistances.add(distanceEntry);
            }
        }

        venueDistanceRepository.saveAll(venueDistances);
    }
}
