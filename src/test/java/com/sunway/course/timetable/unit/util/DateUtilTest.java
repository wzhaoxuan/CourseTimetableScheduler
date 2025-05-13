package com.sunway.course.timetable.unit.util;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sunway.course.timetable.util.date.DateUtils;
import org.junit.jupiter.api.Test;

public class DateUtilTest {

    @Test
    void testGetMonths_shouldReturnAllMonths(){
        List<String> months = DateUtils.getMonths();
        assertEquals(12, months.size());

        assertNotNull(months, "Month list should not be null");

        assertEquals("January", months.get(0));
        assertEquals("February", months.get(1));
        assertEquals("December", months.get(11));
    }

    @Test
    void testGetCurrentYear_shouldReturnCurrentYear(){
        int currentYear = DateUtils.getCurrentYear();
        int expectedYear = Year.now().getValue();
        assertEquals(expectedYear, currentYear);
    }

    @Test
    void testGetYearOptions_shouldReturnCurrentYear(){
        List<String> yearOptions = DateUtils.getYearOptions();
        int currentYear = DateUtils.getCurrentYear();

        assertNotNull(yearOptions, "Year options list should not be null");
        assertEquals(1, yearOptions.size());
        assertEquals(String.valueOf(currentYear), yearOptions.get(0));
    }


}
