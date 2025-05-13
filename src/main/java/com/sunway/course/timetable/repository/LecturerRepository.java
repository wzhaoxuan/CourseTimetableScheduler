package com.sunway.course.timetable.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Lecturer;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    Optional<Lecturer> findById(Long id);
    Optional<Lecturer> findByName(String name);
    Optional<List<Lecturer>> findByType(String type);

}
