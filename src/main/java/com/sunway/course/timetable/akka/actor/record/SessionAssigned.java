package com.sunway.course.timetable.akka.actor.record;
import java.util.List;
import com.sunway.course.timetable.akka.actor.interfaces.SessionAssignmentResponse;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.Student;

public record SessionAssigned(
    Venue venue,
    int dayIndex,
    int startIndex,
    int durationSlots,
    List<Student> assignedStudents
) implements SessionAssignmentResponse {}
