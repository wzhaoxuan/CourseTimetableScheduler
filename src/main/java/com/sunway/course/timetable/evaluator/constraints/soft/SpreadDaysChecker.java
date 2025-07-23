package com.sunway.course.timetable.evaluator.constraints.soft;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

/**
 * Checks if students and lecturers have their sessions spread over too many days.
 * This is a soft constraint, meaning it can be violated but incurs a penalty.
 */
public class SpreadDaysChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Spread Over Too Many Days";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 90.0; 

    }

    /**
     * Calculates the penalty for students and lecturers having sessions spread over more than 3 days.
     * This method iterates through all sessions and checks if a student or lecturer has sessions
     * scheduled on more than 3 different days.
     *
     * @param sessions List of all sessions in the schedule
     * @param sessionVenueMap Map of sessions to their assigned venues (not used here)
     * @return The total penalty for students and lecturers with sessions spread over too many days
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        int penalty = 0;

        // Student side
        Map<Long, Set<String>> studentDays = new HashMap<>();
        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            studentDays.computeIfAbsent(s.getStudent().getId(), k -> new HashSet<>())
                       .add(s.getDay());
        }
        for (Set<String> days : studentDays.values()) {
            if (days.size() > 3) {  // allow up to 3 days, penalize if > 3
                penalty += (days.size() - 3);
            }
        }

        // Lecturer side
        Map<String, Set<String>> lecturerDays = new HashMap<>();
        for (Session s : sessions) {
            if (s.getLecturer() == null) continue;
            lecturerDays.computeIfAbsent(s.getLecturer().getName(), k -> new HashSet<>())
                        .add(s.getDay());
        }
        for (Set<String> days : lecturerDays.values()) {
            if (days.size() > 3) {
                penalty += (days.size() - 3);
            }
        }

        return penalty;
    }
}

