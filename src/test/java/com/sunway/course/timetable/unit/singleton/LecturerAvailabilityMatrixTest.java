package com.sunway.course.timetable.unit.singleton;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;

/**
 * Unit tests for LecturerAvailabilityMatrix class.
 * This class tests the functionality of the LecturerAvailabilityMatrix,
 * including availability checks, assignment and unassignment of slots,
 * auto-registration of lecturers, and resetting the matrix.
 */
public class LecturerAvailabilityMatrixTest {

    private LecturerAvailabilityMatrix matrix;
    private final String lecturerId = "21033105";

    @BeforeEach
    public void setup() {
        matrix = new LecturerAvailabilityMatrix();
        matrix.registerLecturer(lecturerId);
    }

    @Test
    public void testDefaultAvailabilityIsTrue() {
        assertTrue(matrix.isAvailable(lecturerId, 0, 0, 2)); // Monday 8:00–9:00
    }

    @Test
    public void testAssignMakesSlotUnavailable() {
        matrix.assign(lecturerId, 0, 4, 6); // Monday 10:00–11:00
        assertFalse(matrix.isAvailable(lecturerId, 0, 4, 6));
    }

    @Test
    public void testUnassignMakesSlotAvailable() {
        matrix.assign(lecturerId, 1, 8, 10); // Tuesday 12:00–13:00
        assertFalse(matrix.isAvailable(lecturerId, 1, 8, 10));
    }

    @Test
    public void testAutoRegisterDuringAvailabilityCheck() {
        String unregistered = "21033105";
        assertTrue(matrix.isAvailable(unregistered, 2, 0, 1)); // Should auto-register
    }

    @Test
    public void testResetClearsAllAssignments() {
        matrix.assign(lecturerId, 0, 0, 3);
        matrix.reset();
        assertTrue(matrix.isAvailable(lecturerId, 0, 0, 3));
    }

    @Test
    public void testInvalidSlotRangeReturnsFalse() {
        assertFalse(matrix.isAvailable(lecturerId, -1, 0, 1));
        assertFalse(matrix.isAvailable(lecturerId, 0, 19, 21));
    }

    @Test
    public void testGetAssignedDays() {
        matrix.assign(lecturerId, 0, 0, 2); // Monday
        matrix.assign(lecturerId, 2, 0, 2); // Wednesday
        Set<Integer> assignedDays = matrix.getAssignedDays(lecturerId);
        assertTrue(assignedDays.contains(0));
        assertTrue(assignedDays.contains(2));
        assertEquals(2, assignedDays.size());
    }

    @Test
    public void testGetAssignedDaysEmptyForUnregisteredLecturer() {
        Set<Integer> days = matrix.getAssignedDays("ghost");
        assertTrue(days.isEmpty());
    }

    @Test
    public void testGetDailyAvailabilityArrayDefensiveCopy() {
        boolean[] before = matrix.getDailyAvailabilityArray(lecturerId, 1);
        matrix.assign(lecturerId, 1, 5, 7); // Tuesday
        boolean[] after = matrix.getDailyAvailabilityArray(lecturerId, 1);

        assertEquals(20, before.length);
        assertEquals(20, after.length);
        assertTrue(after[5]);
        assertFalse(before[5]);
    }
}

