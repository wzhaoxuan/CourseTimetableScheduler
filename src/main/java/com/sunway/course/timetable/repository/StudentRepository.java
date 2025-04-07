package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Custom query methods can be defined here if needed
    // For example, findByName(String name) to find students by their name
    Optional<Student> findByName(String name);
}
