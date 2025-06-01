package com.sunway.course.timetable.model.assignment;
import java.time.LocalTime;

import com.sunway.course.timetable.model.Venue;


public class SessionAssignmentResult {
    private final String day;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Venue venue;

    public SessionAssignmentResult(String day, LocalTime startTime, LocalTime endTime, Venue venue) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
    }

    public String getDay() { return day; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Venue getVenue() { return venue; }
}

