package com.sunway.course.timetable.akka.actor.record;
import com.sunway.course.timetable.akka.actor.interfaces.VenueCommand;
import com.sunway.course.timetable.akka.protocol.VenueResponse;

import akka.actor.typed.ActorRef;

public record CheckAndAssignSlot(
    int dayIndex,
    int startIndex,
    int endIndex,
    String lecturerId,
    ActorRef<VenueResponse> replyTo
) implements VenueCommand {}
