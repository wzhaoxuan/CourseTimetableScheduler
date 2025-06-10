package com.sunway.course.timetable.interfaces.services;

import java.util.Optional;

import com.sunway.course.timetable.model.WeekDayConstraint;

public interface WeekDayConstraintService {
    Optional<WeekDayConstraint> getWeekDayConstraintByLecturerId(Long lecturerId);
    WeekDayConstraint addWeekDayConstraint(WeekDayConstraint weekDayConstraint);
}
