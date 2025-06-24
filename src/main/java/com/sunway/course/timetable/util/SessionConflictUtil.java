package com.sunway.course.timetable.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.sunway.course.timetable.model.Session;

public class SessionConflictUtil {

    public static int countOverlaps(List<Session> sessions) {
        List<Session> sortedSessions = new ArrayList<>(sessions);
        sortedSessions.sort(Comparator.comparing(Session::getDay).thenComparing(Session::getStartTime));

        int overlaps = 0;
        for (int i = 0; i < sortedSessions.size(); i++) {
            for (int j = i + 1; j < sortedSessions.size(); j++) {
                Session a = sortedSessions.get(i);
                Session b = sortedSessions.get(j);

                // Stop checking if sessions are on different days
                if (!a.getDay().equals(b.getDay())) break;

                // Skip if same module type group (same class, shared students)
                if (Objects.equals(a.getTypeGroup(), b.getTypeGroup())) continue;

                // Count only if overlapping
                if (a.getEndTime().isAfter(b.getStartTime())) {
                    overlaps++;
                }
            }
        }
        return overlaps;
    }
}
