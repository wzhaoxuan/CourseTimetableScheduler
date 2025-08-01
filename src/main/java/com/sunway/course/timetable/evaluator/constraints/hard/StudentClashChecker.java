package com.sunway.course.timetable.evaluator.constraints.hard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.util.SessionConflictUtil;


/**
 * Checks for student clashes in the timetable.
 * A clash occurs when a student is scheduled for multiple sessions at the same time.
 */
public class StudentClashChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Student Clashes";
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
     * Calculates the penalty for student clashes.
     * This method groups sessions by student and counts the number of overlapping sessions for each student.
     * 
     * @param sessions List of sessions to check for clashes
     * @param sessionVenueMap Map of sessions to their assigned venues (not used in this checker)
     * @return Total penalty for student clashes, which is the sum of overlaps for each student
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<Long, List<Session>> byStudent = new HashMap<>();

        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            byStudent.computeIfAbsent(s.getStudent().getId(), k -> new ArrayList<>()).add(s);
        }

        return byStudent.values().stream()
            .mapToInt(SessionConflictUtil::countOverlaps)
            .sum();
    }
}



