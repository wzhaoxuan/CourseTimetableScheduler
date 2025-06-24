package com.sunway.course.timetable.unit.util;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.venuedistance.VenueDistance;
import com.sunway.course.timetable.repository.VenueDistanceRepository;
import com.sunway.course.timetable.repository.VenueRepository;
import com.sunway.course.timetable.service.generator.VenueDistanceGenerator;
import com.sunway.course.timetable.util.VenueDistanceUtils;


@ExtendWith(MockitoExtension.class)
public class VenueDistanceUtilsTest {

    @Mock private VenueRepository venueRepository;
    @Mock private VenueDistanceRepository venueDistanceRepository;

    @InjectMocks private VenueDistanceGenerator generator;

    @Test
    public void testGenerateVenueDistances_savesAllDistances() {
        Venue v1 = new Venue("Lecture Hall", "JC1", 200, "University West", "3");
        Venue v2 = new Venue("Lecture Hall", "JC2", 200, "University West", "3");
        List<Venue> venues = List.of(v1, v2);

        when(venueRepository.findAll()).thenReturn(venues);

        generator.generateVenueDistances();

        ArgumentCaptor<List<VenueDistance>> captor = ArgumentCaptor.forClass(List.class);
        verify(venueDistanceRepository).saveAll(captor.capture());

        List<VenueDistance> result = captor.getValue();
        assertEquals(4, result.size()); // 2x2 grid
    }

    @Test
    public void testCalculateDistance_sameVenue() {
        Venue v = new Venue("Lecture Hall", "JC2", 200, "University West", "3");
        assertEquals(0.0, VenueDistanceUtils.calculateDistance(v, v));
    }

    @Test
    public void testExtractFloorLevel_withText() {
        assertEquals(2, VenueDistanceUtils.extractFloorLevel("Level 2"));
        assertEquals(0, VenueDistanceUtils.extractFloorLevel("Ground"));
        assertEquals(0, VenueDistanceUtils.extractFloorLevel("Invalid"));
    }

    @Test
    public void testSanitizeVenueName() {
        assertEquals("JC1", VenueDistanceUtils.sanitizeVenueName(" jc1 "));
        assertNull(VenueDistanceUtils.sanitizeVenueName(null));
    }
}

