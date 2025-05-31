package com.sunway.course.timetable.akka.actor.record;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.akka.protocol.VenueResponse;

public record VenueRejected(Venue venue, String reason) implements VenueResponse {}
