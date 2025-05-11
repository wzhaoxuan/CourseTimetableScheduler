package com.sunway.course.timetable.interfaces.services;

import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.Venue;

public interface VenueService {
    List<Venue> getAllVenues();
    Optional<Venue> getVenueByName(String name);
    Optional<Venue> getVenueById(Long id);
    Venue addVenue(Venue venue);
    void publishVenueAddedEvent(String name);
}
