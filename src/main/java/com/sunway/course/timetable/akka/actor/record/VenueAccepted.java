package com.sunway.course.timetable.akka.actor.record;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.akka.protocol.VenueResponse;

public record VenueAccepted(Venue venue, int dayIndex, int startIndex, int durationSlots)
    implements VenueResponse {}
