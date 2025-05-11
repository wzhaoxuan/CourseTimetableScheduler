package com.sunway.course.timetable.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.repository.SatisfactionRepository;
import com.sunway.course.timetable.interfaces.services.StatisfactionService;

@Service
public class StatisfactionServiceImpl implements StatisfactionService{

    private final SatisfactionRepository satisfactionRepository;

    public StatisfactionServiceImpl(SatisfactionRepository satisfactionRepository) {
        this.satisfactionRepository = satisfactionRepository;
    }

    @Override
    public List<Satisfaction> getAllSatisfactions() {
        return satisfactionRepository.findAll();
    }

    @Override
    public Satisfaction getSatisfactionById(Long id) {
        return satisfactionRepository.findById(id).orElse(null);
    }

    @Override
    public Satisfaction saveSatisfaction(Satisfaction satisfaction) {
        return satisfactionRepository.save(satisfaction);
    }

    @Override
    public void deleteSatisfaction(Long id) {
        satisfactionRepository.deleteById(id);
    }

}
