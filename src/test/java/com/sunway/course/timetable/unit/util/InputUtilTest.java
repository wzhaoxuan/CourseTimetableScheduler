package com.sunway.course.timetable.unit.util;
import java.time.Month;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.util.InputUtil;

public class InputUtilTest {

    @Test
    public void testGetIntake() {
        List<String> intakes = InputUtil.getIntake();
        assertEquals(List.of("JANUARY", "APRIL", "SEPTEMBER"), intakes);
    }

    @Test
    public void testParseMonth_valid() {
        assertEquals(Month.JANUARY, InputUtil.parseMonth("January"));
        assertEquals(Month.JANUARY, InputUtil.parseMonth("january"));
        assertEquals(Month.JANUARY, InputUtil.parseMonth("January 2025"));
    }

    @Test
    public void testParseMonth_invalidOrEmpty() {
        assertNull(InputUtil.parseMonth("Invalid"));
        assertNull(InputUtil.parseMonth(""));
        assertNull(InputUtil.parseMonth(null));
    }

    @Test
    public void testGetCurrentYear() {
        int year = InputUtil.getCurrentYear();
        assertEquals(Year.now().getValue(), year);
    }

    @Test
    public void testGetYearOptions() {
        int current = Year.now().getValue();
        List<String> expected = List.of(
                String.valueOf(current - 3),
                String.valueOf(current - 2),
                String.valueOf(current - 1),
                String.valueOf(current)
        );
        assertEquals(expected, InputUtil.getYearOptions());
    }
}

