package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    // Define methods for CRUD operations and custom queries here
    // For example, findByName(String name) to find venues by their name
    Optional<Venue> findByType(String type);
    Optional<Venue> findByCapacity(int capacity);
    Optional<Venue> findByFloorType(String floorType);
    Optional<Venue> findByFloor(String floor);



}
