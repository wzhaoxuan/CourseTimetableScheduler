package com.sunway.course.timetable.service;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.WeekDayConstraintService;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.repository.WeekDayConstraintRepository;


@Service
public class WeekDayConstraintServiceImpl implements WeekDayConstraintService {

    private final WeekDayConstraintRepository weekDayConstraintRepository;
    private final LecturerServiceImpl lecturerService;

    public WeekDayConstraintServiceImpl(WeekDayConstraintRepository weekDayConstraintRepository,
                                        LecturerServiceImpl lecturerService) {
        this.weekDayConstraintRepository = weekDayConstraintRepository;
        this.lecturerService = lecturerService;
    }

    @Override
    public Optional<WeekDayConstraint> getWeekDayConstraintByLecturerId(Long lecturerId) {
        return weekDayConstraintRepository.findByLecturer_Id(lecturerId);
    }

    @Override
    public WeekDayConstraint addWeekDayConstraint(WeekDayConstraint weekDayConstraint) {
        return weekDayConstraintRepository.save(weekDayConstraint);
    }
}
