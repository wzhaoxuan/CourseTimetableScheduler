package com.sunway.course.timetable.interfaces.services;

import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.Lecturer;

public interface LecturerService {
    Optional<Lecturer> getLecturerById(Long id);
    List<Lecturer> getAllLecturers();
    Optional<Lecturer> getLecturerByName(String name);
    Optional<List<Lecturer>> getLecturersByType(String type);

}
