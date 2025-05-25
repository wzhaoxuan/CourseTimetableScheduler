package com.sunway.course.timetable.service;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.time.DayOfWeek;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.event.LecturerConstraintConfirmedEvent;
import com.sunway.course.timetable.interfaces.services.WeekDayConstraintService;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.repository.WeekDayConstraintRepository;
import com.sunway.course.timetable.exception.ValueNotFoundException;

import javafx.scene.control.CheckBox;


@Service
public class WeekDayConstraintServiceImpl implements WeekDayConstraintService {

    private final WeekDayConstraintRepository weekDayConstraintRepository;
    private final LecturerServiceImpl lecturerService;
    private final ApplicationEventPublisher eventPublisher;

    public WeekDayConstraintServiceImpl(WeekDayConstraintRepository weekDayConstraintRepository,
                                        LecturerServiceImpl lecturerService,
                                        ApplicationEventPublisher eventPublisher) {
        this.weekDayConstraintRepository = weekDayConstraintRepository;
        this.lecturerService = lecturerService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<WeekDayConstraint> getWeekDayConstraintByLecturerId(Long lecturerId) {
        return weekDayConstraintRepository.findByLecturer_Id(lecturerId);
    }

    @Override
    public WeekDayConstraint addWeekDayConstraint(WeekDayConstraint weekDayConstraint) {
        return weekDayConstraintRepository.save(weekDayConstraint);
    }

    @Override
    public void selectWeedayConstraint(String lecturerIdText, 
                                        CheckBox monday, 
                                        CheckBox tuesday, 
                                        CheckBox wednesday, 
                                        CheckBox thursday, 
                                        CheckBox friday) {
        // Convert the string to a Long and find the lecturer
        try {
                Long lecturerIdLong = Long.parseLong(lecturerIdText);
                Lecturer lecturer = findLecturerById(lecturerIdLong);
                if (lecturer == null) return;
        
                WeekDayConstraint constraint = getWeekDayConstraintByLecturerId(lecturerIdLong).orElse(new WeekDayConstraint());
                updateConstraintWithUIValues(constraint, lecturer, monday, tuesday, wednesday, thursday, friday);
                addWeekDayConstraint(constraint);
        
                System.out.println("Availability saved for Lecturer ID: " + lecturerIdLong);

                // Build list of available days (selected checkboxes)
                List<String> unavailableDays = new ArrayList<>();
                if (monday.isSelected()) unavailableDays.add("MONDAY");
                if (tuesday.isSelected()) unavailableDays.add("TUESDAY");
                if (wednesday.isSelected()) unavailableDays.add("WEDNESDAY");
                if (thursday.isSelected()) unavailableDays.add("THURSDAY");
                if (friday.isSelected()) unavailableDays.add("FRIDAY");

                //Add button to weekdayGrid in GenerateTimetableController
                eventPublisher.publishEvent(new LecturerConstraintConfirmedEvent(lecturer, unavailableDays));
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid Lecturer ID. Must be a number.");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private Lecturer findLecturerById(Long id) {
        Optional<Lecturer> lecturer = lecturerService.getLecturerById(id);

        if (lecturer.isEmpty()){
            System.out.println("Lecturer not found with ID: " + id);
            return null; // Exit if the lecturer is not found
        }

        return lecturer.get(); // Return the found lecturer
    }

    private void updateConstraintWithUIValues(WeekDayConstraint constraint, 
                                                Lecturer lecturer,
                                                CheckBox monday,
                                                CheckBox tuesday,
                                                CheckBox wednesday,
                                                CheckBox thursday,
                                                CheckBox friday) {
        constraint.setLecturer(lecturer); // Set the lecturer for the constraint
        constraint.setMonday(monday.isSelected());
        constraint.setTuesday(tuesday.isSelected());
        constraint.setWednesday(wednesday.isSelected());
        constraint.setThursday(thursday.isSelected());
        constraint.setFriday(friday.isSelected());
    }
}
