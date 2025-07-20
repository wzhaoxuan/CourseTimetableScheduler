package com.sunway.course.timetable.unit.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

/**
 * Unit tests for VenueAvailabilityMatrix class.
 * This class tests the functionality of the VenueAvailabilityMatrix,
 * including time conversion, day indexing, availability checks,
 * assignment and unassignment of venues, and finding suitable venues.
 */

public class VenueAvailabilityMatrixTest {

    private VenueAvailabilityMatrix matrix;
    private Venue venueA;
    private Venue venueB;

    @BeforeEach
    public void setup() {
        venueA = new Venue(1L,"Lecture Hall", "JC1", 200, "University West", "3"); 
        venueB = new Venue(2L,"Lecture Hall", "JC2", 200, "University West", "3");

        // matrix = new VenueAvailabilityMatrix(List.of(venueA, venueB));
        matrix.initialize(); // Must call this to build internal structure
    }

    @Test
    public void testIsAvailableTrueByDefault() {
        int start = 0;
        int end = 2;
        int day = 0;
        assertTrue(matrix.isAvailable(venueA, start, end, day));
    }

    @Test
    public void testAssignVenue() {
        int start = 4;
        int end = 6;
        int day = 1;

        matrix.assign(venueA, start, end, day);
        assertFalse(matrix.isAvailable(venueA, start, end, day));
    }

    @Test
    public void testResetClearsAllAssignments() {
        matrix.assign(venueA, 0, 2, 0);
        assertFalse(matrix.isAvailable(venueA, 0, 2, 0));

        matrix.reset();
        assertTrue(matrix.isAvailable(venueA, 0, 2, 0));
    }

    @Test
    public void testGetIndexForVenue() {
        assertEquals(0, matrix.getIndexForVenue(venueA));
        assertEquals(1, matrix.getIndexForVenue(venueB));
    }
}
