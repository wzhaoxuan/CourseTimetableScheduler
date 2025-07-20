package com.sunway.course.timetable.unit.singleton;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;

public class StudentAvailabilityMatrixTest {

    private StudentAvailabilityMatrix matrix;
    private Long studentId1;
    private Long studentId2;

    @BeforeEach
    public void setup() {
        matrix = new StudentAvailabilityMatrix();

        // Fake students
        Student s1 = new Student();
        s1.setId(1L);
        Student s2 = new Student();
        s2.setId(2L);

        matrix.initializeStudents(List.of(s1, s2));
        studentId1 = 1L;
        studentId2 = 2L;
    }

    @Test
    public void testIsAvailableInitiallyTrue() {
        assertTrue(matrix.isAvailable(studentId1, 0, 0, 2)); // Monday 8:00–9:00
    }

    @Test
    public void testAssignStudent() {
        matrix.assign(studentId1, 1, 4, 3); // Tuesday 10:00–11:30
        assertFalse(matrix.isAvailable(studentId1, 1, 4, 3));

    }


    @Test
    public void testResetRestoresAvailability() {
        matrix.assign(studentId1, 0, 1, 2);
        assertFalse(matrix.isAvailable(studentId1, 0, 1, 2));

        matrix.reset();
        assertTrue(matrix.isAvailable(studentId1, 0, 1, 2));
    }

    @Test
    public void testGetAssignedTimes() {
        matrix.assign(studentId1, 0, 0, 2); // Monday 8:00–9:00

        List<LocalTime> times = matrix.getAssignedDays(studentId1, 0);
        assertEquals(2, times.size());
        assertEquals(LocalTime.of(8, 0), times.get(0));
        assertEquals(LocalTime.of(8, 30), times.get(1));
    }

    @Test
    public void testWouldBeOnlySessionTrue() {
        // Student has no other sessions
        assertTrue(matrix.wouldBeOnlySession(studentId1, 0, 0, 2));
    }

    @Test
    public void testWouldBeOnlySessionFalse() {
        matrix.assign(studentId1, 0, 6, 2); // already has 11:00–12:00
        assertFalse(matrix.wouldBeOnlySession(studentId1, 0, 4, 2)); // new session 10:00–11:00
    }

    @Test
    public void testWouldBeOnlySessionTrueAfterMarkingSameBlock() {
        matrix.assign(studentId1, 0, 4, 2); // 10:00–11:00
        assertTrue(matrix.wouldBeOnlySession(studentId1, 0, 4, 2)); // same block again
    }

    @Test
    public void testInvalidStudentReturnsFalse() {
        assertFalse(matrix.isAvailable(99L, 0, 0, 2));
        assertFalse(matrix.wouldBeOnlySession(99L, 0, 0, 1));
        assertTrue(matrix.getAssignedDays(99L, 0).isEmpty());
    }
}
