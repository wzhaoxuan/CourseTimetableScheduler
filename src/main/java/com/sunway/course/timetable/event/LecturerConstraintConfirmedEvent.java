package com.sunway.course.timetable.event;
import java.util.List;
import com.sunway.course.timetable.model.Lecturer;

public class LecturerConstraintConfirmedEvent {
    private final Lecturer lecturer;
    private List<String> unavailableDays;

    public LecturerConstraintConfirmedEvent(Lecturer lecturer, List<String> unavailableDays) {
        this.lecturer = lecturer;
        this.unavailableDays = unavailableDays;
    }

      public Lecturer getLecturer() {
        return lecturer;
    }

    public List<String> getUnavailableDays() {
        return unavailableDays;
    }
}
