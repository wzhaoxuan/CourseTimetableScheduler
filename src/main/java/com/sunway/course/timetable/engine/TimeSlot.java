package com.sunway.course.timetable.engine;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimeSlot {
    private DayOfWeek day;
    private LocalTime startTime;
    private LocalTime endTime;

    public TimeSlot(DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public boolean overlapsWith(TimeSlot other) {
        if (this.day != other.day) return false; // Different days do not overlap
        return !this.startTime.isAfter(other.endTime) && !this.endTime.isBefore(other.startTime);
    }

    // Override equals and hashCode for proper comparison in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;

        TimeSlot timeSlot = (TimeSlot) o;

        if (day != timeSlot.day) return false;
        if (!startTime.equals(timeSlot.startTime)) return false;
        return endTime.equals(timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        int result = day.hashCode();
        result = 31 * result + startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("TimeSlot{day=%s, start=%s, end=%s}", day, startTime, endTime);
    }

}
