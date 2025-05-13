package com.sunway.course.timetable.interfaces.services;

import java.util.Optional;

import com.sunway.course.timetable.model.WeekDayConstraint;

import javafx.scene.control.CheckBox;

public interface WeekDayConstraintService {
    Optional<WeekDayConstraint> getWeekDayConstraintByLecturerId(Long lecturerId);
    WeekDayConstraint addWeekDayConstraint(WeekDayConstraint weekDayConstraint);
    void selectWeedayConstraint(String lecturerIdText, CheckBox monday, CheckBox tuesday, 
                                        CheckBox wednesday, CheckBox thursday, CheckBox friday);
}
