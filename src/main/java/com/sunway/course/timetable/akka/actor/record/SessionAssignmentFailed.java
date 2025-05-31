package com.sunway.course.timetable.akka.actor.record;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentResponse;

public record SessionAssignmentFailed(String reason) implements SessionAssignmentResponse {}
