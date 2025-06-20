package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.SatisfactionService;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.repository.SatisfactionRepository;

@Service
public class SatisfactionServiceImpl implements SatisfactionService{

    private final SatisfactionRepository satisfactionRepository;

    public SatisfactionServiceImpl(SatisfactionRepository satisfactionRepository) {
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
        if (satisfaction == null) {
            throw new IllegalArgumentException("Satisfaction object cannot be null.");
        }

        if (satisfaction.getScore() < 0.0 || satisfaction.getScore() > 100.0) {
            throw new IllegalArgumentException("Fitness score must be between 0 and 100.");
        }

        if (satisfaction.getConflict() < 0) {
            throw new IllegalArgumentException("Conflict count cannot be negative.");
        }

        boolean exists = satisfactionRepository.existsByScheduleHash(satisfaction.getScheduleHash());
        if (exists) {
            // Optionally return existing version instead
            System.out.println("Duplicate schedule hash detected, skipping save: " + satisfaction.getScheduleHash());
            return null;
        }
        return satisfactionRepository.save(satisfaction);
    }

    @Override
    public void deleteSatisfaction(Long id) {
        satisfactionRepository.deleteById(id);
    }

    @Override
    public Optional<Satisfaction> findLatestSatisfaction() {
        return satisfactionRepository.findTopByOrderByIdDesc();
    }

    public String getNextVersionTag() {
        List<Satisfaction> all = satisfactionRepository.findAll();
        int maxVersion = all.stream()
            .map(Satisfaction::getVersionTag)
            .filter(tag -> tag != null && tag.startsWith("v"))
            .mapToInt(tag -> Integer.parseInt(tag.substring(1)))
            .max()
            .orElse(0);
        return "v" + (maxVersion + 1);
    }
}
