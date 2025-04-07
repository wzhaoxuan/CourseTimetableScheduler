package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Satisfaction;

@Repository
public interface SatisfactionRepository extends JpaRepository<Satisfaction, Long> {
    // Custom query methods can be defined here if needed
    // For example, findByName(String name) to find satisfactions by their name
    Optional<Satisfaction> findByName(String name);

}
