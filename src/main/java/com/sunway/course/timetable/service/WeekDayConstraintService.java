package com.sunway.course.timetable.service;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.event.LecturerConstraintConfirmedEvent;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.repository.WeekDayConstraintRepository;

import javafx.scene.control.CheckBox;


@Service
public class WeekDayConstraintService {

    @Autowired
    private LecturerService lecturerService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final WeekDayConstraintRepository weekDayConstraintRepository;

    @Autowired
    public WeekDayConstraintService(WeekDayConstraintRepository weekDayConstraintRepository) {
        this.weekDayConstraintRepository = weekDayConstraintRepository;
    }

    public Optional<WeekDayConstraint> getByLecturerId(Long lecturerId) {
        return weekDayConstraintRepository.findByLecturer_Id(lecturerId);
    }

    public WeekDayConstraint addWeekDayConstraint(WeekDayConstraint weekDayConstraint) {
        return weekDayConstraintRepository.save(weekDayConstraint);
    }

    public void deleteWeekDayConstraint(Long id) {
        weekDayConstraintRepository.deleteById(id);
    }

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
        
                WeekDayConstraint constraint = getByLecturerId(lecturerIdLong).orElse(new WeekDayConstraint());
                updateConstraintWithUIValues(constraint, lecturer, monday, tuesday, wednesday, thursday, friday);
                addWeekDayConstraint(constraint);
        
                System.out.println("Availability saved for Lecturer ID: " + lecturerIdLong);

                //Add button to weekdayGrid in GenerateTimetableController
                eventPublisher.publishEvent(new LecturerConstraintConfirmedEvent(lecturer));
                
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
