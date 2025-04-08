package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;

@Repository
public interface VenueDistanceRepository extends JpaRepository<VenueDistance, VenueDistanceId> {
    // Custom query methods can be defined here if needed

    Optional<VenueDistance> findById(VenueDistanceId id);

}
