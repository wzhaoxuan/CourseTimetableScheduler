package com.sunway.course.timetable.interfaces.services;

import java.util.Optional;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.WeekDayConstraint;

import javafx.scene.control.CheckBox;

public interface WeekDayConstraintService {
    Optional<WeekDayConstraint> getByLecturerId(Long lecturerId);
    WeekDayConstraint addWeekDayConstraint(WeekDayConstraint weekDayConstraint);
    void deleteWeekDayConstraint(Long id);
    void selectWeedayConstraint(String lecturerIdText, CheckBox monday, CheckBox tuesday, 
                                        CheckBox wednesday, CheckBox thursday, CheckBox friday);
}
