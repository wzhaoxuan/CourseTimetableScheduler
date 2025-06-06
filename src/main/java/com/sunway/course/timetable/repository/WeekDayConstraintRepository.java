package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.WeekDayConstraint;

@Repository
public interface WeekDayConstraintRepository extends JpaRepository<WeekDayConstraint, Long> {
    Optional<WeekDayConstraint> findByLecturer_Id(Long lecturerId); // Fetch by foreign key
}
