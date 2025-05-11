package com.sunway.course.timetable.service;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.sunway.course.timetable.exception.CreationException;
import com.sunway.course.timetable.exception.IdNotFoundException;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.repository.VenueRepository;

@ExtendWith(MockitoExtension.class)
public class VenueServiceTest {

    @Mock private VenueRepository venueRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private VenueServiceImpl venueService;

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
        List<Venue> result = venueService.getAllVenues();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test Get All Venues -- Failure")
    void testGetAllVenuesFailure() {
        when(venueRepository.findAll())
        .thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            venueService.getAllVenues();
        });

        assertEquals("Database error", exception.getMessage());
        verify(venueRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test Get Venue By ID -- Success")
    void testGetVenueById() {
        Venue venue = new Venue();
        venue.setId(1L);

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        Optional<Venue> result = venueService.getVenueById(1L);

        assertTrue(result.isPresent());
        verify(venueRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test Get Venue By ID -- Not Found")
    void testGetVenueByIdNotFound() {

        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Venue> result = venueService.getVenueById(1L);

        assertTrue(result.isEmpty());
        verify(venueRepository, times(1)).findById(1L);
    }


    @Test
    @DisplayName("Test Get Venue By Name -- Success")
    void testGetVenueByName(){
        Venue venue = new Venue();
        venue.setName("UW2-10");

        when(venueRepository.findByName("UW2-10")).thenReturn(Optional.of(venue));
        assertTrue(venueService.getVenueByName("UW2-10").isPresent());
        assertEquals("UW2-10", venueService.getVenueByName("UW2-10").get().getName());
        verify(venueRepository, times(2)).findByName("UW2-10");
    }

    @Test
    @DisplayName("Test Get Venue By Name -- Not Found")
    void testGetVenueByNameNotFound(){
        when(venueRepository.findByName("UW2-10")).thenReturn(Optional.empty());
        Optional<Venue> result = venueService.getVenueByName("UW2-10");
        assertTrue(result.isEmpty());
        verify(venueRepository, times(1)).findByName("UW2-10");
    }

    @Test
    @DisplayName("Test Add Venue -- Success")
    void testAddVenue() {
        Venue venue = new Venue();
        venue.setName("UW2-10");
        venue.setCapacity(100);
        venue.setFloor("Level 2");
        venue.setFloorType("Uni West");
        venue.setType("Room");

        when(venueRepository.save(any(Venue.class)))
        .thenReturn(venue);

        Venue result = venueService.addVenue(venue);

        assertNotNull(result);
        assertEquals("UW2-10", result.getName());

        verify(venueRepository, times(1)).save(venue);
    }

    @Test
    @DisplayName("Test Add Venue -- Failure")
    void testAddVenueFailure() {
        Venue venue = new Venue();
        venue.setName("UW2-10");
        venue.setCapacity(100);
        venue.setFloor("Level 2");
        venue.setFloorType("Uni West");
        venue.setType("Room");

        when(venueRepository.save(any(Venue.class)))
        .thenThrow(new CreationException("Failed to add venue"));

        CreationException exception = assertThrows(CreationException.class, () -> {
            venueService.addVenue(venue);
        });

        assertEquals("Failed to add venue", exception.getMessage());
        verify(venueRepository, times(1)).save(venue);
    }

    // @Test
    // @DisplayName("Test Publish Venue Added Event -- Success")
    // void testPublishVenueAddedEvent() {
    //     Venue venue = new Venue();
    //     venue.setName("UW2-10");
    //     venue.setCapacity(100);
    //     venue.setFloor("Level 2");
    //     venue.setFloorType("Uni West");
    //     venue.setType("Room");

    //     when(venueRepository.findByName("UW2-10")).thenReturn(Optional.of(venue));

    //     venueService.publishVenueAddedEvent("UW2-10");

    //     verify(eventPublisher, times(1)).publishEvent(new VenueAddedEvent(venue));
    // }

    // @Test
    // @DisplayName("Test Publish Venue Added Event -- Not Found")
    // void testPublishVenueAddedEventNotFound() {
    //     when(venueRepository.findByName("UW2-10")).thenReturn(Optional.empty());

    //     venueService.publishVenueAddedEvent("UW2-10");

    //     verify(eventPublisher, times(0)).publishEvent(new VenueAddedEvent(new Venue()));
    // }


}
