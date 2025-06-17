package com.sunway.course.timetable.unit.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.model.venuedistance.VenueDistanceId;
import com.sunway.course.timetable.repository.VenueDistanceRepository;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;

@ExtendWith(MockitoExtension.class)
public class VenueDistanceServiceTest {

    @Mock
    private VenueDistanceRepository venueDistanceRepository;

    @InjectMocks
    private VenueDistanceServiceImpl venueDistanceService;

    private VenueDistance venueDistance;
    private VenueDistanceId venueDistanceId;

    @BeforeEach
    void setUp() {
        venueDistanceId = new VenueDistanceId("UW2-10", "UC7-9");
        venueDistance = new VenueDistance();
        venueDistance.setVenueDistanceId(venueDistanceId);
    }

    @Test
    @DisplayName("Test getAllVenueDistances returns list")
    void testGetAllVenueDistances() {
        List<VenueDistance> venueDistances = List.of(venueDistance);
        when(venueDistanceRepository.findAll()).thenReturn(venueDistances);

        List<VenueDistance> result = venueDistanceService.getAllVenueDistances();

        assertEquals(1, result.size());
        verify(venueDistanceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getAllVenueDistances returns empty list")
    void testGetAllVenueDistancesEmpty() {
        when(venueDistanceRepository.findAll()).thenReturn(Collections.emptyList());

        List<VenueDistance> result = venueDistanceService.getAllVenueDistances();

        assertTrue(result.isEmpty());
        verify(venueDistanceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getVenueDistanceById returns venue distance")
    void testGetVenueDistanceById() {
        when(venueDistanceRepository.findById(venueDistanceId)).thenReturn(Optional.of(venueDistance));

        VenueDistance result = venueDistanceService.getVenueDistanceById(venueDistanceId);

        assertNotNull(result);
        assertEquals(venueDistanceId, result.getVenueDistanceId());
        verify(venueDistanceRepository, times(1)).findById(venueDistanceId);
    }

    @Test
    @DisplayName("Test getVenueDistanceById returns null when not found")
    void testGetVenueDistanceByIdNotFound() {
        when(venueDistanceRepository.findById(venueDistanceId)).thenReturn(Optional.empty());

        VenueDistance result = venueDistanceService.getVenueDistanceById(venueDistanceId);

        assertNull(result);
        verify(venueDistanceRepository, times(1)).findById(venueDistanceId);
    }

    @Test
    @DisplayName("Test saveVenueDistance persists correctly")
    void testSaveVenueDistance() {
        when(venueDistanceRepository.save(venueDistance)).thenReturn(venueDistance);

        VenueDistance saved = venueDistanceService.saveVenueDistance(venueDistance);

        assertNotNull(saved);
        assertEquals(venueDistanceId, saved.getVenueDistanceId());
        verify(venueDistanceRepository, times(1)).save(venueDistance);
    }

    @Test
    @DisplayName("Test deleteVenueDistance calls repository delete")
    void testDeleteVenueDistance() {
        doNothing().when(venueDistanceRepository).deleteById(venueDistanceId);

        venueDistanceService.deleteVenueDistance(venueDistanceId);

        verify(venueDistanceRepository, times(1)).deleteById(venueDistanceId);
    }
}
