package com.sunway.course.timetable.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.programme.Programme;
import com.sunway.course.timetable.model.programme.ProgrammeId;

@Repository
public interface ProgrammeRepository extends JpaRepository<Programme, ProgrammeId> {
    Optional<Programme> findByName(String name); // Example method to find by name
    Optional<Programme> findById(ProgrammeId id); // Example method to find by ID
    List<Programme> findByYear(int year);
    List<Programme> findByIntake(String intake);
    List<Programme> findBySemester(int semester);
    List<Programme> findByModuleId(String moduleCode); 
}
