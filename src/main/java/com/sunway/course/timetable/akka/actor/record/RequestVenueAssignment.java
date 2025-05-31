package com.sunway.course.timetable.akka.actor.record;
import akka.actor.typed.ActorRef;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentResponse;
import com.sunway.course.timetable.akka.actor.interfaces.VenueCoordinatorCommand;


public record RequestVenueAssignment(
    int durationHours,
    int minCapacity,
    ActorRef<SessionAssignmentResponse> replyTo
) implements VenueCoordinatorCommand {}
