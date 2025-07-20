package com.sunway.course.timetable.evaluator.constraints.hard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

public class DuplicateTypeGroupChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Duplicate Session Types Per Module";
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
     * Checks if there are multiple sessions of the same type group scheduled
     * for the same day and start time. This is a hard constraint, meaning it must be satisfied.
     * 
     * @param sessions List of all sessions in the schedule
     * @param sessionVenueMap Map of sessions to their assigned venues (not used here)
     * @return The total penalty for duplicate type groups
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<String, Set<String>> typeGroupScheduleMap = new HashMap<>();

        for (Session s : sessions) {
            String typeGroup = s.getTypeGroup();
            if (typeGroup == null) continue;

            String slotKey = s.getDay() + "-" + s.getStartTime();
            typeGroupScheduleMap
                .computeIfAbsent(typeGroup, k -> new HashSet<>())
                .add(slotKey);
        }

        int violations = 0;
        for (Set<String> times : typeGroupScheduleMap.values()) {
            if (times.size() > 1) {
                violations += times.size() - 1;
            }
        }

        return violations;
    }
}
