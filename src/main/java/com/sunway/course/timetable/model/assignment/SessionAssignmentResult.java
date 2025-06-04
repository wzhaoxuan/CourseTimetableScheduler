package com.sunway.course.timetable.model.assignment;
import java.time.LocalTime;
import java.util.List;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.Student;


public class SessionAssignmentResult {
    private final String day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Venue venue;
    private final List<Student> assignedStudents;

    public SessionAssignmentResult(String day, LocalTime startTime, LocalTime endTime, Venue venue,
                                   List<Student> assignedStudents) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.assignedStudents = assignedStudents;
    }

    public String getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Venue getVenue() { return venue; }
    public List<Student> getAssignedStudents() { return assignedStudents; }
}

