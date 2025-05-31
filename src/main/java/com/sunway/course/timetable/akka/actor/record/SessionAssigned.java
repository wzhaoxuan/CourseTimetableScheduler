package com.sunway.course.timetable.akka.actor.record;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentResponse;

public record SessionAssigned(
    Venue venue,
    int dayIndex,
    int startIndex,
    int durationSlots
) implements SessionAssignmentResponse {}
