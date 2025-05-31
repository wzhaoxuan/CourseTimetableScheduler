package com.sunway.course.timetable.unit.service;

import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignmentId;
import com.sunway.course.timetable.repository.VenueAssignmentRepository;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VenueAssignmentServiceTest {

    @Mock
    private VenueAssignmentRepository venueAssignmentRepository;

    @InjectMocks
    private VenueAssignmentServiceImpl venueAssignmentService;

    private VenueAssignment assignment;
    private VenueAssignmentId assignmentId;

    @BeforeEach
    void setUp() {
        assignmentId = new VenueAssignmentId(1L, 1L);
        assignment = new VenueAssignment();
        assignment.setVenueAssignmentId(assignmentId);
    }

    @Test
    @DisplayName("Test getAllAssignments returns list")
    void testGetAllAssignments() {
        List<VenueAssignment> assignments = List.of(assignment);
        when(venueAssignmentRepository.findAll()).thenReturn(assignments);

        List<VenueAssignment> result = venueAssignmentService.getAllAssignments();

        assertEquals(1, result.size());
        verify(venueAssignmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getAssignmentById returns present optional")
    void testGetAssignmentById() {
        when(venueAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

        Optional<VenueAssignment> result = venueAssignmentService.getAssignmentById(assignmentId);

        assertTrue(result.isPresent());
        assertEquals(assignmentId, result.get().getVenueAssignmentId());
        verify(venueAssignmentRepository, times(1)).findById(assignmentId);
    }

    @Test
    @DisplayName("Test getAssignmentById returns empty optional")
    void testGetAssignmentByIdNotFound() {
        when(venueAssignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        Optional<VenueAssignment> result = venueAssignmentService.getAssignmentById(assignmentId);

        assertFalse(result.isPresent());
        verify(venueAssignmentRepository, times(1)).findById(assignmentId);
    }

    @Test
    @DisplayName("Test saveAssignment persists correctly")
    void testSaveAssignment() {
        when(venueAssignmentRepository.save(assignment)).thenReturn(assignment);

        VenueAssignment saved = venueAssignmentService.saveAssignment(assignment);

        assertNotNull(saved);
        assertEquals(assignmentId, saved.getVenueAssignmentId());
        verify(venueAssignmentRepository, times(1)).save(assignment);
    }

    @Test
    @DisplayName("Test deleteAssignment calls repository delete")
    void testDeleteAssignment() {
        doNothing().when(venueAssignmentRepository).deleteById(assignmentId);

        venueAssignmentService.deleteAssignment(assignmentId);

        verify(venueAssignmentRepository, times(1)).deleteById(assignmentId);
    }
}
