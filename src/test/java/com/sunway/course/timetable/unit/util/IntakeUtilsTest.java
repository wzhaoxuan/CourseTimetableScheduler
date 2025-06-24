package com.sunway.course.timetable.unit.util;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.util.IntakeUtils;

public class IntakeUtilsTest {

    @Test
    void testSameSemesterReturnsSameIntakeYear() {
        String result = IntakeUtils.getIntakeLabel(1, "January", 2024);
        assertEquals("January-2024", result);
    }

    @Test
    void testCircularBackOneSemester() {
        assertEquals("April-2024", IntakeUtils.getIntakeLabel(2, "September", 2024));
        assertEquals("January-2024", IntakeUtils.getIntakeLabel(2, "April", 2024));
        assertEquals("September-2023", IntakeUtils.getIntakeLabel(2, "January", 2024));
    }

    @Test
    void testCircularBackThreeSemesters() {
        assertEquals("September-2023", IntakeUtils.getIntakeLabel(4, "September", 2024));
        assertEquals("April-2023", IntakeUtils.getIntakeLabel(5, "September", 2024));
    }

    @Test
    void testCaseInsensitiveIntake() {
        assertEquals("January-2024", IntakeUtils.getIntakeLabel(1, "january", 2024));
        assertEquals("April-2024", IntakeUtils.getIntakeLabel(1, "APRIL", 2024));
        assertEquals("September-2024", IntakeUtils.getIntakeLabel(1, "sEpTeMber", 2024));
    }

    @Test
    void testInvalidIntakeThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            IntakeUtils.getIntakeLabel(1, "June", 2024);
        });
        assertTrue(exception.getMessage().contains("Invalid intake"));
    }

    @Test
    void testCapitalize() {
        assertEquals("January", invokeCapitalize("january"));
        assertEquals("April", invokeCapitalize("APRIL"));
        assertEquals("September", invokeCapitalize("september"));
        assertNull(invokeCapitalize(null));
        assertEquals("", invokeCapitalize(""));
    }

    // Helper to access private method using reflection
    private String invokeCapitalize(String input) {
        try {
            var method = IntakeUtils.class.getDeclaredMethod("capitalize", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
