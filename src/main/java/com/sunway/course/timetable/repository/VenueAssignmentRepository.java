package com.sunway.course.timetable.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;


@Repository
public interface VenueAssignmentRepository extends JpaRepository<VenueAssignment, VenueAssignmentId>{
    Optional<VenueAssignment> findBySessionId(Long sessionId);

}
