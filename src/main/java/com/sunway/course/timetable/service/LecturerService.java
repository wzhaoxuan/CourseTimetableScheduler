package com.sunway.course.timetable.service;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.repository.LecturerRepository;

@Service
public class LecturerService {

    @Autowired
    private LecturerRepository lecturerRepository;

    public Optional<Lecturer> getLecturerById(Long id) {
        return lecturerRepository.findById(id);
    }

    public List<Lecturer> getAllLecturers() {
        return lecturerRepository.findAll();
    }

    public Optional<Lecturer> getLecturerByName(String name) {
        return lecturerRepository.findByName(name);
    }

    public Optional<List<Lecturer>> getLecturersByType(String type) {
        return lecturerRepository.findByType(type);
    }

}
