package com.sunway.course.timetable.akka.actor.record;
import akka.actor.typed.ActorRef;
import com.sunway.course.timetable.akka.actor.interfaces.VenueCoordinatorCommand;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentResponse;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentCommand;


public record AssignSession(
    int durationHours,
    int minCapacity,
    ActorRef<VenueCoordinatorCommand> coordinator,
    ActorRef<SessionAssignmentResponse> replyTo
) implements SessionAssignmentCommand {}
