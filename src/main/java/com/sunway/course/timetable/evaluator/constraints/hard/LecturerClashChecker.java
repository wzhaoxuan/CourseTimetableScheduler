package com.sunway.course.timetable.evaluator.constraints.hard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.util.SessionConflictUtil;


/**
 * Checks for lecturer clashes in the timetable.
 * A clash occurs when a lecturer is scheduled for multiple sessions at the same time.
 */
public class LecturerClashChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Lecturer Clashes";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.HARD;
    }

    @Override
    public double getWeight() {
        return 1000.0;
    }

    /**
     * Calculates the penalty for lecturer clashes.
     * This method groups sessions by lecturer and counts the number of overlapping sessions for each lecturer.
     * 
     * @param sessions List of sessions to check for clashes
     * @param sessionVenueMap Map of sessions to their assigned venues (not used in this checker)
     * @return Total penalty for lecturer clashes, which is the sum of overlaps for each lecturer
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        int clashes = 0;
        Map<String, List<Session>> byLecturer = new HashMap<>();

        for (Session s : sessions) {
            if (s.getLecturer() == null) continue;
            byLecturer
                .computeIfAbsent(s.getLecturer().getName(), k -> new ArrayList<>())
                .add(s);
        }

        for (List<Session> lecturerSessions : byLecturer.values()) {
            // Deduplicate by day + start + end time
            List<Session> distinct = lecturerSessions.stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                            .comparing(Session::getDay)
                            .thenComparing(Session::getStartTime)
                            .thenComparing(Session::getEndTime)
                        )
                    ),
                    ArrayList::new
                ));

            clashes += SessionConflictUtil.countOverlaps(distinct);
        }

        return clashes;
    }
}
