package com.sunway.course.timetable.evaluator.constraints.soft;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

public class LongBreakChecker implements ConstraintChecker {

    private static final Logger log = LoggerFactory.getLogger(LongBreakChecker.class);

    @Override
    public String getName() {
        return "Long Breaks (>2 hours)";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 100.0;  // You can tune this based on how important you want
    }

    /**
     * Calculates the penalty for long breaks (gaps > 2 hours) in the schedule.
     * This method checks both students and lecturers for long breaks in their schedules.
     * 
     * @param sessions List of all sessions in the schedule
     * @param sessionVenueMap Map of sessions to their assigned venues (not used here)
     * @return The total penalty for long breaks
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        int violations = 0;

        // Check for Students
        Map<Long, Map<String, List<Session>>> studentSchedule = new HashMap<>();
        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            studentSchedule.computeIfAbsent(s.getStudent().getId(), k -> new HashMap<>())
                .computeIfAbsent(s.getDay(), d -> new ArrayList<>())
                .add(s);
        }

        violations += countLongBreaks(studentSchedule);

        // Check for Lecturers
        Map<String, Map<String, List<Session>>> lecturerSchedule = new HashMap<>();
        for (Session s : sessions) {
            if (s.getLecturer() == null) continue;
            lecturerSchedule.computeIfAbsent(s.getLecturer().getName(), k -> new HashMap<>())
                .computeIfAbsent(s.getDay(), d -> new ArrayList<>())
                .add(s);
        }

        violations += countLongBreaks(lecturerSchedule);

        return violations;
    }

    /**
     * Counts the number of long breaks (gaps > 2 hours) in the schedule map.
     * 
     * @param scheduleMap Map of schedules grouped by key (student ID or lecturer name)
     * @param <K> Type of the key (e.g., Long for student ID, String for lecturer name)
     * @return The count of long breaks in the schedule
     */
    private <K> int countLongBreaks(Map<K, Map<String, List<Session>>> scheduleMap) {
        int count = 0;
        for (Map<String, List<Session>> dayMap : scheduleMap.values()) {
            for (List<Session> daySessions : dayMap.values()) {
                daySessions.sort(Comparator.comparing(Session::getStartTime));
                for (int i = 0; i < daySessions.size() - 1; i++) {
                    Session curr = daySessions.get(i);
                    Session next = daySessions.get(i + 1);
                    long gap = Duration.between(curr.getEndTime(), next.getStartTime()).toMinutes();
                    if (gap > 120) count++;
                }
            }
        }
        return count;
    }
}
