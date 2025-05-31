package com.sunway.course.timetable.engine;
import java.time.DayOfWeek;
import java.time.LocalTime;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.venuedistance.VenueDistance;

public class TimeSlot {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;
    private Venue venue;
    private VenueDistance venueDistance; // optional, null if unknown

    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime, Venue venue) {
        this(day, startTime, endTime, venue, null);
    }

    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime, Venue venue, VenueDistance venueDistance) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.venueDistance = venueDistance;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public VenueDistance getVenueDistance() {
        return venueDistance;
    }

    public void setVenueDistance(VenueDistance venueDistance) {
        this.venueDistance = venueDistance;
    }

    public boolean overlapsWith(TimeSlot other) {
        if (this.day != other.day) return false; // Different days do not overlap
        return !this.startTime.isAfter(other.endTime) && !this.endTime.isBefore(other.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;

        TimeSlot timeSlot = (TimeSlot) o;

        if (day != timeSlot.day) return false;
        if (!startTime.equals(timeSlot.startTime)) return false;
        if (!endTime.equals(timeSlot.endTime)) return false;
        return venue != null ? venue.equals(timeSlot.venue) : timeSlot.venue == null;
    }

    @Override
    public int hashCode() {
        int result = day.hashCode();
        result = 31 * result + startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        result = 31 * result + (venue != null ? venue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("TimeSlot{day=%s, start=%s, end=%s, venue=%s, distance=%s}",
                day, startTime, endTime, venue != null ? venue.getName() : "null",
                venueDistance != null ? venueDistance.getDistance() : "N/A");
    }

}
