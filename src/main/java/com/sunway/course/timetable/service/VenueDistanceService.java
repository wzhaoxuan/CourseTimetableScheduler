package com.sunway.course.timetable.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;
import com.sunway.course.timetable.repository.VenueDistanceRepository;

@Service
public class VenueDistanceService {

    private final VenueDistanceRepository venueDistanceRepository;

    public VenueDistanceService(VenueDistanceRepository venueDistanceRepository) {
        this.venueDistanceRepository = venueDistanceRepository;
    }

    public List<VenueDistance> getAllVenueDistances() {
        return venueDistanceRepository.findAll();
    }

    public VenueDistance getVenueDistanceById(VenueDistanceId id) {
        return venueDistanceRepository.findById(id).orElse(null);
    }

    public VenueDistance saveVenueDistance(VenueDistance venueDistance) {
        return venueDistanceRepository.save(venueDistance);
    }

    public void deleteVenueDistance(VenueDistanceId id) {
        venueDistanceRepository.deleteById(id);
    }

}
