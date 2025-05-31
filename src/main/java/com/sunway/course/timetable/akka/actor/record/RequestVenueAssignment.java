package com.sunway.course.timetable.akka.actor.record;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentResponse;
import com.sunway.course.timetable.akka.actor.interfaces.VenueCoordinatorCommand;

import akka.actor.typed.ActorRef;


public record RequestVenueAssignment(
    int durationHours,
    int minCapacity,
    String lecturerId,
    ActorRef<SessionAssignmentResponse> replyTo
) implements VenueCoordinatorCommand {}
