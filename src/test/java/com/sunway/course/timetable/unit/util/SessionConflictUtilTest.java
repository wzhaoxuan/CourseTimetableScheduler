package com.sunway.course.timetable.unit.util;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.util.SessionConflictUtil;

public class SessionConflictUtilTest {

    private Session createSession(String day, String start, String end, String typeGroup) {
        Session s = new Session();
        s.setDay(day);
        s.setStartTime(LocalTime.parse(start));
        s.setEndTime(LocalTime.parse(end));
        s.setTypeGroup(typeGroup);
        return s;
    }

    @Test
    void testNoOverlapSameDay() {
        Session s1 = createSession("Monday", "08:00", "09:00", "G1");
        Session s2 = createSession("Monday", "09:00", "10:00", "G2");
        assertEquals(0, SessionConflictUtil.countOverlaps(List.of(s1, s2)));
    }

    @Test
    void testOverlapSameDayDifferentGroup() {
        Session s1 = createSession("Monday", "08:00", "09:30", "G1");
        Session s2 = createSession("Monday", "09:00", "10:00", "G2");
        assertEquals(1, SessionConflictUtil.countOverlaps(List.of(s1, s2)));
    }

    @Test
    void testOverlapSameDaySameGroup() {
        Session s1 = createSession("Monday", "08:00", "09:30", "G1");
        Session s2 = createSession("Monday", "09:00", "10:00", "G1");
        assertEquals(0, SessionConflictUtil.countOverlaps(List.of(s1, s2)));
    }

    @Test
    void testDifferentDays() {
        Session s1 = createSession("Monday", "08:00", "09:00", "G1");
        Session s2 = createSession("Tuesday", "08:30", "09:30", "G2");
        assertEquals(0, SessionConflictUtil.countOverlaps(List.of(s1, s2)));
    }

    @Test
    void testMultipleOverlaps() {
        Session s1 = createSession("Monday", "08:00", "10:00", "G1");
        Session s2 = createSession("Monday", "09:00", "11:00", "G2");
        Session s3 = createSession("Monday", "09:30", "10:30", "G3");
        assertEquals(3, SessionConflictUtil.countOverlaps(List.of(s1, s2, s3)));
        // s1-s2, s1-s3, s2-s3 = 3 overlaps
    }
}

