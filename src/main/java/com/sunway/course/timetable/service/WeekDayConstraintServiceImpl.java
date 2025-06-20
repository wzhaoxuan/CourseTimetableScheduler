package com.sunway.course.timetable.service;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.WeekDayConstraintService;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.repository.WeekDayConstraintRepository;


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
}
