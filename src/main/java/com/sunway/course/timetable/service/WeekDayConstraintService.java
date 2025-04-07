package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.exception.IdNotFoundException;
import com.sunway.course.timetable.model.WeekDayConstraint;
import com.sunway.course.timetable.repository.WeekDayConstraintRepository;


@Service
public class WeekDayConstraintService {

    private final WeekDayConstraintRepository weekDayConstraintRepository;

    @Autowired
    public WeekDayConstraintService(WeekDayConstraintRepository weekDayConstraintRepository) {
        this.weekDayConstraintRepository = weekDayConstraintRepository;
    }

    public List<WeekDayConstraint> getAllWeekDayConstraints() {
        return weekDayConstraintRepository.findAll();
    }

    public Optional<WeekDayConstraint> getWeekDayConstraintById(Long id) {
        return weekDayConstraintRepository.findById(id);
    }

    public WeekDayConstraint addWeekDayConstraint(WeekDayConstraint weekDayConstraint) {
        return weekDayConstraintRepository.save(weekDayConstraint);
    }

    public void deleteWeekDayConstraint(Long id) {
        weekDayConstraintRepository.deleteById(id);
    }

    public WeekDayConstraint updateWeekDayConstraint(Long id, WeekDayConstraint weekDayConstraint) {
        if (weekDayConstraintRepository.existsById(id)) {
            weekDayConstraint.setId(id);
            return weekDayConstraintRepository.save(weekDayConstraint);
        } else {
            throw new IdNotFoundException("WeekDayConstraint not found with id: " + id);
        }
    }

}
