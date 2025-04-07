package com.sunway.course.timetable.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.repository.SatisfactionRepository;

@Service
public class StatisfactionService {

    private final SatisfactionRepository satisfactionRepository;

    public StatisfactionService(SatisfactionRepository satisfactionRepository) {
        this.satisfactionRepository = satisfactionRepository;
    }

    public List<Satisfaction> getAllSatisfactions() {
        return satisfactionRepository.findAll();
    }

    public Satisfaction getSatisfactionById(Long id) {
        return satisfactionRepository.findById(id).orElse(null);
    }

    public Satisfaction saveSatisfaction(Satisfaction satisfaction) {
        return satisfactionRepository.save(satisfaction);
    }

    public void deleteSatisfaction(Long id) {
        satisfactionRepository.deleteById(id);
    }

}
