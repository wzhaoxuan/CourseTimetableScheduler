package com.sunway.course.timetable.unit.util;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.util.DateUtil;

public class DateUtilTest {

    @Test
    void testGetMonths_shouldReturnAllMonths(){
        List<String> months = DateUtil.getIntake();
        assertEquals(12, months.size());

        assertNotNull(months, "Month list should not be null");

        assertEquals("JANUARY", months.get(0));
        assertEquals("FEBRUARY", months.get(1));
        assertEquals("DECEMBER", months.get(11));
    }

    @Test
    void testGetCurrentYear_shouldReturnCurrentYear(){
        int currentYear = DateUtil.getCurrentYear();
        int expectedYear = Year.now().getValue();
        assertEquals(expectedYear, currentYear);
    }

    @Test
    void testGetYearOptions_shouldReturnCurrentYear(){
        List<String> yearOptions = DateUtil.getYearOptions();
        int currentYear = DateUtil.getCurrentYear();

        assertNotNull(yearOptions, "Year options list should not be null");
        assertEquals(1, yearOptions.size());
        assertEquals(String.valueOf(currentYear), yearOptions.get(0));
    }


}
