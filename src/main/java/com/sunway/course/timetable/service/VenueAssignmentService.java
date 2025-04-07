package com.sunway.course.timetable.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;
import com.sunway.course.timetable.repository.VenueAssignmentRepository;

@Service
public class VenueAssignmentService {

    private final VenueAssignmentRepository venueAssignmentRepository;

    @Autowired
    public VenueAssignmentService(VenueAssignmentRepository repository) {
        this.venueAssignmentRepository = repository;
    }

    public List<VenueAssignment> getAllAssignments() {
        return venueAssignmentRepository.findAll();
    }

    public Optional<VenueAssignment> getAssignmentById(VenueAssignmentId key) {
        return venueAssignmentRepository.findById(key);
    }

    public VenueAssignment saveAssignment(VenueAssignment assignment) {
        return venueAssignmentRepository.save(assignment);
    }

    public void deleteAssignment(VenueAssignmentId key) {
        venueAssignmentRepository.deleteById(key);
    }
}
