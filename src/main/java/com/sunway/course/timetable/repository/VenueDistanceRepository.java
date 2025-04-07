package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;

@Repository
public interface VenueDistanceRepository extends JpaRepository<VenueDistance, VenueDistanceId> {
    // Custom query methods can be defined here if needed
    // For example, findByName(String name) to find venue distances by their name
    Optional<VenueDistance> findByName(String name);
    Optional<VenueDistance> findById(VenueDistanceId id);

}
