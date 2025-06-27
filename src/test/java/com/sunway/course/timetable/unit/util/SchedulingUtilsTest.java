package com.sunway.course.timetable.unit.util;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;
import com.sunway.course.timetable.util.SchedulingUtils;


public class SchedulingUtilsTest {

    @Test
    @DisplayName("validateSemesterCapacity passes when all semesters fit within max venue capacity")
    void testValidateSemesterCapacity_success() {
        // Students 1–5 in sem 1, students 6–8 in sem 2
        Set<Student> sem1 = new HashSet<>();
        for (long i = 1; i <= 5; i++) {
            Student s = new Student();
            s.setId(i);
            sem1.add(s);
        }
        Set<Student> sem2 = new HashSet<>();
        for (long i = 6; i <= 8; i++) {
            Student s = new Student();
            s.setId(i);
            sem2.add(s);
        }

        ModuleAssignmentData d1 = new ModuleAssignmentData(null, null, List.of(), sem1);
        ModuleAssignmentData d2 = new ModuleAssignmentData(null, null, List.of(), sem2);
        List<ModuleAssignmentData> moduleData = List.of(d1, d2);

        // Map student → semester
        Map<Long,Integer> studentSem = new HashMap<>();
        sem1.forEach(s -> studentSem.put(s.getId(), 1));
        sem2.forEach(s -> studentSem.put(s.getId(), 2));

        // Venues: largest capacity = 5
        List<Venue> venues = List.of(
            new Venue(1L, "Room", "A", 5, "", ""),
            new Venue(2L, "Room", "B", 10, "", "")
        );

        // Should not throw
        assertDoesNotThrow(() ->
            SchedulingUtils.validateSemesterCapacity(moduleData, studentSem, venues)
        );
    }

    @Test
    @DisplayName("validateSemesterCapacity throws if any semester exceeds max venue capacity")
    void testValidateSemesterCapacity_failure() {
        // 4 students in sem 1
        Set<Student> sem1 = new HashSet<>();
        for (long i = 1; i <= 5; i++) {
            Student s = new Student();
            s.setId(i);
            sem1.add(s);
        }
        ModuleAssignmentData d = new ModuleAssignmentData(null, null, List.of(), sem1);
        List<ModuleAssignmentData> moduleData = List.of(d);

        Map<Long,Integer> studentSem = new HashMap<>();
        sem1.forEach(s -> studentSem.put(s.getId(), 1));

        // Only one small venue capacity=3
        List<Venue> venues = List.of(new Venue(1L, "Room", "X", 3, "", ""));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            SchedulingUtils.validateSemesterCapacity(moduleData, studentSem, venues)
        );
        assertTrue(ex.getMessage().contains("Semester 1 has 5 students but largest venue holds only 3"));
    }

    @Test
    @DisplayName("recordTeachingHours accumulates hours and enforces MAX_TEACHING_HOURS")
    void testRecordTeachingHours() {
        Map<String,Integer> hours = new HashMap<>();
        // initial 18 hours
        hours.put("DrA", 18);

        // adding 2 stays within MAX_TEACHING_HOURS=20
        assertDoesNotThrow(() ->
            SchedulingUtils.recordTeachingHours(hours, "DrA", 2)
        );
        assertEquals(20, hours.get("DrA"));

        // adding 1 more exceeds limit
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            SchedulingUtils.recordTeachingHours(hours, "DrA", 1)
        );
        assertTrue(ex.getMessage().contains("DrA would exceed max teaching hours: 20 + 1 > 20"));
    }

    @Test
    @DisplayName("validateLecturerWeekdays does not throw when unavailable days = MAX_UNAVAILABLE_DAYS")
    void testValidateLecturerWeekdays_atLimit() {
        LecturerDayAvailabilityUtil util = mock(LecturerDayAvailabilityUtil.class);
        long lecturerId = 42L;
        String lecturerName = "Dr. AtLimit";

        List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        int max = SchedulingUtils.MAX_UNAVAILABLE_DAYS;

        // Stub first 'max' days as unavailable, rest available
        for (int i = 0; i < max; i++) {
            when(util.isUnavailable(lecturerId, days.get(i))).thenReturn(true);
        }
        for (int i = max; i < days.size(); i++) {
            when(util.isUnavailable(lecturerId, days.get(i))).thenReturn(false);
        }

        // Should not throw because count == max
        assertDoesNotThrow(() ->
            SchedulingUtils.validateLecturerWeekdays(util, lecturerId, lecturerName)
        );
    }

    @Test
    @DisplayName("validateLecturerWeekdays throws when unavailable days > MAX_UNAVAILABLE_DAYS")
    void testValidateLecturerWeekdays_overLimit() {
        LecturerDayAvailabilityUtil util = mock(LecturerDayAvailabilityUtil.class);
        long lecturerId = 99L;
        String lecturerName = "Dr. OverLimit";

        List<String> days = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        int max = SchedulingUtils.MAX_UNAVAILABLE_DAYS;

        // Stub first 'max + 1' days as unavailable, rest available
        for (int i = 0; i <= max; i++) {
            when(util.isUnavailable(lecturerId, days.get(i))).thenReturn(true);
        }
        for (int i = max + 1; i < days.size(); i++) {
            when(util.isUnavailable(lecturerId, days.get(i))).thenReturn(false);
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            SchedulingUtils.validateLecturerWeekdays(util, lecturerId, lecturerName)
        );

        String expected = String.format(
            "Lecturer %s is unavailable on %d weekdays (max %d).",
            lecturerName, max + 1, max
        );
        assertEquals(expected, ex.getMessage());
    }
}

