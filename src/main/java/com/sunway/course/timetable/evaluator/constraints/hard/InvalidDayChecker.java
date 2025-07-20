package com.sunway.course.timetable.evaluator.constraints.hard;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

public class InvalidDayChecker implements ConstraintChecker {

    

    @Override
    public String getName() {
        return "Sessions on Invalid Days";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.HARD;
    }

    @Override
    public double getWeight() {
        return 1000.0;
    }

    private static final Set<String> VALID_DAYS = Set.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

    /**
     * Checks if any session is scheduled on an invalid day.
     * An invalid day is defined as any day not in the set of valid days.
     * 
     * @param sessions List of sessions to check
     * @param sessionVenueMap Map of sessions to their assigned venues (not used in this checker)
     * @return The number of sessions scheduled on invalid days
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        return sessions.stream()
            .filter(s -> !VALID_DAYS.contains(s.getDay()))
            .count();
    }
}
