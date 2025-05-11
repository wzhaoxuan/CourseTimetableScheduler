package com.sunway.course.timetable.interfaces.services;

import java.util.List;
import java.util.Optional;

import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;

public interface VenueAssignmentService {
    List<VenueAssignment> getAllAssignments();
    Optional<VenueAssignment> getAssignmentById(VenueAssignmentId key);
    VenueAssignment saveAssignment(VenueAssignment assignment);
    void deleteAssignment(VenueAssignmentId key);

}
