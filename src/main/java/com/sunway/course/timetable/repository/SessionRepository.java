package com.sunway.course.timetable.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    // Custom query methods can be defined here if needed
    // For example, findByName(String name) to find sessions by their name
    // Optional<Session> findByName(String name);

}
