package com.sunway.course.timetable.service.venue;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;
import com.sunway.course.timetable.repository.VenueDistanceRepository;
import com.sunway.course.timetable.interfaces.services.VenueDistanceService;

@Service
public class VenueDistanceServiceImpl implements VenueDistanceService {

    private final VenueDistanceRepository venueDistanceRepository;

    public VenueDistanceServiceImpl(VenueDistanceRepository venueDistanceRepository) {
        this.venueDistanceRepository = venueDistanceRepository;
    }

    @Override
    public List<VenueDistance> getAllVenueDistances() {
        return venueDistanceRepository.findAll();
    }

    @Override
    public VenueDistance getVenueDistanceById(VenueDistanceId id) {
        return venueDistanceRepository.findById(id).orElse(null);
    }

    @Override
    public VenueDistance saveVenueDistance(VenueDistance venueDistance) {
        return venueDistanceRepository.save(venueDistance);
    }

    @Override
    public void deleteVenueDistance(VenueDistanceId id) {
        venueDistanceRepository.deleteById(id);
    }
}
