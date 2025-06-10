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
    // private final ActorSystem<VenueCoordinatorActor.Command> venueCoordinatorActor;
    // private final ActorSystem<?> actorSystem;  // for ask pattern scheduling

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
        Optional<VenueAssignment> existing = venueAssignmentRepository.findById(assignment.getVenueAssignmentId());
        return existing.orElseGet(() -> venueAssignmentRepository.save(assignment));
    }

    @Override
    public void deleteAssignment(VenueAssignmentId key) {
        venueAssignmentRepository.deleteById(key);
    }

    public Optional<Venue> getVenueBySessionId(Long sessionId) {
        List<VenueAssignment> assignments = venueAssignmentRepository.findBySessionId(sessionId);
        return assignments.isEmpty() ? Optional.empty() : Optional.of(assignments.get(0).getVenue());
    }
}
