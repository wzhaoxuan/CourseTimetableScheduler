package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.LecturerService;
import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.repository.LecturerRepository;

@Service
public class LecturerServiceImpl implements LecturerService {

    private final LecturerRepository lecturerRepository;

    public LecturerServiceImpl(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    @Override
    public Optional<Lecturer> getLecturerById(Long id) {
        return lecturerRepository.findById(id);
    }

    @Override
    public List<Lecturer> getAllLecturers() {
        return lecturerRepository.findAll();
    }

    @Override
    public Optional<Lecturer> getLecturerByName(String name) {
        return lecturerRepository.findByName(name);
    }

    @Override
    public Optional<List<Lecturer>> getLecturersByType(String type) {
        return lecturerRepository.findByType(type);
    } 
}
