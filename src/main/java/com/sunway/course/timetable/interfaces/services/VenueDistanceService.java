package com.sunway.course.timetable.interfaces.services;

import java.util.List;

import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;

public interface VenueDistanceService {
    List<VenueDistance> getAllVenueDistances();
    VenueDistance getVenueDistanceById(VenueDistanceId id);
    VenueDistance saveVenueDistance(VenueDistance venueDistance);
    void deleteVenueDistance(VenueDistanceId id);
}
