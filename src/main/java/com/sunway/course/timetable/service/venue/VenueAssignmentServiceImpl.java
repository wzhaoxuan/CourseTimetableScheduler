package com.sunway.course.timetable.service.venue;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.interfaces.services.VenueAssignmentService;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;
import com.sunway.course.timetable.repository.VenueAssignmentRepository;


@Service
public class VenueAssignmentServiceImpl implements VenueAssignmentService {

    private final VenueAssignmentRepository venueAssignmentRepository;

    public VenueAssignmentServiceImpl(VenueAssignmentRepository repository) {
        this.venueAssignmentRepository = repository;
    }

    @Override
    public List<VenueAssignment> getAllAssignments() {
        return venueAssignmentRepository.findAll();
    }

    @Override
    public Optional<VenueAssignment> getAssignmentById(VenueAssignmentId key) {
        return venueAssignmentRepository.findById(key);
    }

    @Override
    public VenueAssignment saveAssignment(VenueAssignment assignment) {
        return venueAssignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(VenueAssignmentId key) {
        venueAssignmentRepository.deleteById(key);
    }

    public Optional<Venue> getVenueBySessionId(Long sessionId) {
        List<VenueAssignment> assignments = venueAssignmentRepository.findBySession_Id(sessionId);
        return assignments.isEmpty() ? Optional.empty() : Optional.of(assignments.get(0).getVenue());
    }
    public Optional<VenueAssignment> getAssignmentBySessionIdAndVersionTag(Long sessionId, String versionTag) {
        return venueAssignmentRepository.findBySession_IdAndVenueAssignmentId_VersionTag(sessionId, versionTag);
    }
}
