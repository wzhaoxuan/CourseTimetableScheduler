package com.sunway.course.timetable.akka.actor.record;
import java.util.List;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;

public record SessionCandidate(
    int day,
    int startSlot,
    int endSlot,
    Venue venue,
    String lecturer,
    List<Student> students,
    String sessionType
) {}
