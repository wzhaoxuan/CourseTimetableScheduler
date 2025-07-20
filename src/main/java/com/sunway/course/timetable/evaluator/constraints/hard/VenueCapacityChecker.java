package com.sunway.course.timetable.evaluator.constraints.hard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

/**
 * Checks if sessions exceed the capacity of their assigned venues.
 * This is a hard constraint, meaning it must be satisfied.
 * A penalty is applied for each session that exceeds the venue's capacity.
 */
public class VenueCapacityChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Venue Over Capacity";
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
     * Calculates the penalty for sessions that exceed the capacity of their assigned venues.
     * This method iterates through all sessions and checks if the number of sessions
     * of the same type, day, and start time exceeds the venue's capacity.
     * 
     * @param sessions List of all sessions in the schedule
     * @param sessionVenueMap Map of sessions to their assigned venues
     * @return The total penalty for sessions that exceed venue capacities
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<String, Integer> sessionGroupSize = new HashMap<>();

        for (Session s : sessions) {
            String key = s.getTypeGroup() + "-" + s.getDay() + "-" + s.getStartTime();
            sessionGroupSize.put(key, sessionGroupSize.getOrDefault(key, 0) + 1);
        }

        int overCap = 0;
        for (Session s : sessions) {
            Venue v = sessionVenueMap.get(s);
            if (v == null) continue;

            String key = s.getTypeGroup() + "-" + s.getDay() + "-" + s.getStartTime();
            int groupSize = sessionGroupSize.getOrDefault(key, 0);

            String sessionType = s.getType().toLowerCase();
            String venueType = v.getType() != null ? v.getType().toLowerCase() : "";

            boolean isLecture = sessionType.contains("lecture");
            boolean isPracticalTutorialWorkshop =
                sessionType.contains("practical") ||
                sessionType.contains("tutorial") ||
                sessionType.contains("workshop");

            boolean lectureAllowed = isLecture && (venueType.contains("auditorium") || venueType.contains("lecture theatre"));
            boolean ptwAllowed = isPracticalTutorialWorkshop && (venueType.contains("room") || venueType.contains("lab"));

            if (groupSize > v.getCapacity() && !(lectureAllowed || ptwAllowed)) {
                overCap++;
            }
        }

        return overCap;
    }
}
