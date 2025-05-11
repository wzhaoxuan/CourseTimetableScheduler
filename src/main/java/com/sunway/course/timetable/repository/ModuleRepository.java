package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Module;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    // Custom query methods can be defined here if needed
    // For example, findByName(String name) to find modules by their name
    Optional<Module> findByName(String name);
    Optional<Module> findById(Long id);
    Optional<Module> findByCreditHour(int creditHour);

}
