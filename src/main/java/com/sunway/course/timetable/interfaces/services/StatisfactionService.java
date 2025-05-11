package com.sunway.course.timetable.interfaces.services;

import java.util.List;

import com.sunway.course.timetable.model.Satisfaction;

public interface StatisfactionService {
    List<Satisfaction> getAllSatisfactions();
    Satisfaction getSatisfactionById(Long id);
    Satisfaction saveSatisfaction(Satisfaction satisfaction);
    void deleteSatisfaction(Long id);
}
