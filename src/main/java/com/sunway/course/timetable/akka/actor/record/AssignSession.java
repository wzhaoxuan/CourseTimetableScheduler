package com.sunway.course.timetable.akka.actor.record;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentCommand;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentResponse;
import com.sunway.course.timetable.akka.actor.interfaces.VenueCoordinatorCommand;

import akka.actor.typed.ActorRef;


public record AssignSession(
    int durationHours,
    int minCapacity,
    String lecturerId,
    ActorRef<VenueCoordinatorCommand> coordinator,
    ActorRef<SessionAssignmentResponse> replyTo
) implements SessionAssignmentCommand {}
