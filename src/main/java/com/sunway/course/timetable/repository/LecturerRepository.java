package com.sunway.course.timetable.repository;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Lecturer;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    // Define methods for CRUD operations and custom queries here
    // For example, findByName(String name) to find lecturers by their name
    Optional<Lecturer> findByName(String name);

    Optional<List<Lecturer>> findByType(String type);

}
