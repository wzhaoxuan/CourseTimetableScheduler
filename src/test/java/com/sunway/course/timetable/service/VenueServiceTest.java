package com.sunway.course.timetable.service;
import org.junit.jupiter.api.DisplayName; 
import com.sunway.course.timetable.event.VenueAddedEvent;
import com.sunway.course.timetable.exception.IdNotFoundException;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VenueServiceTest {

    @Mock private VenueRepository venueRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private VenueService venueService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test Get All Venues -- Success")
    void testGetAllVenues() {
        List<Venue> venues = new ArrayList<>();

        Venue venue = new Venue();
        venue.setName("UW2-10");
        venue.setCapacity(100);
        venue.setFloor("Level 2");
        venue.setFloorType("Uni West");
        venue.setType("Room");
        venues.add(venue);

        Venue venue2 = new Venue();
        venue2.setName("UW2-11");
        venue2.setCapacity(35);
        venue2.setFloor("Level 2");
        venue2.setFloorType("Uni West");
        venue2.setType("Room");
        venues.add(venue2);

        when(venueRepository.findAll()).thenReturn(venues);

        List<Venue> result = venueService.getAllVenues();

        assertEquals(2, result.size());
        verify(venueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get All Venues -- Empty List")
    void testGetAllVenuesEmpty() {
        List<Venue> venues = new ArrayList<>();

        when(venueRepository.findAll()).thenReturn(venues);

        List<Venue> result = venueService.getAllVenues();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test Get All Venues -- Failure")
    void testGetAllVenuesFailure() {
        when(venueRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            venueService.getAllVenues();
        });

        assertEquals("Database error", exception.getMessage());
    }

    @Test
    @DisplayName("Test Get Venue By ID -- Success")
    void testGetVenueById() {
        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("UW2-10");
        venue.setCapacity(100);
        venue.setFloor("Level 2");
        venue.setFloorType("Uni West");
        venue.setType("Room");

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        Optional<Venue> result = venueService.getVenueById(1L);

        assertTrue(result.isPresent());
        assertEquals("UW2-10", result.get().getName());
    }

    @Test
    @DisplayName("Test Get Venue By ID -- Not Found")
    void testGetVenueByIdNotFound() {
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Venue> result = venueService.getVenueById(1L);

        assertFalse(result.isPresent());
    }
}
