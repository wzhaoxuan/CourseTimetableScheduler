package com.sunway.course.timetable.akka.actor.record;
import akka.actor.typed.ActorRef;
import com.sunway.course.timetable.akka.actor.interfaces.VenueCoordinatorCommand;
import com.sunway.course.timetable.akka.protocol.VenueResponse;
import com.sunway.course.timetable.model.Venue;

public record VenueAssignmentAttempt(
    Venue venue,
    int dayIndex,
    int startIndex,
    int durationSlots,
    ActorRef<VenueResponse> replyTo
) implements VenueCoordinatorCommand {}
