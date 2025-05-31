package com.sunway.course.timetable.akka.actor.record;
import akka.actor.typed.ActorRef;
import com.sunway.course.timetable.akka.actor.interfaces.VenueCommand;
import com.sunway.course.timetable.akka.protocol.VenueResponse;

public record CheckAndAssignSlot(
    int dayIndex,
    int startIndex,
    int endIndex,
    ActorRef<VenueResponse> replyTo
) implements VenueCommand {}
