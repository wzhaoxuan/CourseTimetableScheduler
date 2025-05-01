package com.sunway.course.timetable.event;

import com.sunway.course.timetable.model.Venue;

public class VenueAddedEvent {
    private final Venue venue;

    public VenueAddedEvent(Venue venue) {
        this.venue = venue;
    }

    public Venue getVenue() {
        return venue;
    }
}
