package com.sunway.course.timetable.evaluator.constraints.hard;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

public class InvalidDayChecker implements ConstraintChecker {

    private static final Set<String> VALID_DAYS = Set.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");

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
        return 500.0;
    }

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        return sessions.stream()
            .filter(s -> !VALID_DAYS.contains(s.getDay()))
            .count();
    }
}
