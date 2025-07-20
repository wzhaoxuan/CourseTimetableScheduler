package com.sunway.course.timetable.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;


@Repository
public interface VenueAssignmentRepository extends JpaRepository<VenueAssignment, VenueAssignmentId> {
    List<VenueAssignment> findBySession_Id(Long sessionId);
    Optional<VenueAssignment> findBySession_IdAndVenueAssignmentId_VersionTag(Long sessionId,String versionTag);
}
