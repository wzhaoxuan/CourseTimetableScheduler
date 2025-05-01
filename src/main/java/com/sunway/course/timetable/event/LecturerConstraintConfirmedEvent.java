package com.sunway.course.timetable.event;

import com.sunway.course.timetable.model.Lecturer;

public class LecturerConstraintConfirmedEvent {
    private final Lecturer lecturer;

    public LecturerConstraintConfirmedEvent(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

      public Lecturer getLecturer() {
        return lecturer;
    }
}
