package com.sunway.course.timetable.evaluator.constraints.hard;

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
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;

/**
 * Checks for venue transitions greater than 300 meters between consecutive sessions for the same student on the same day.
 * This is a soft constraint, meaning it can be violated but incurs a penalty.
 */
public class VenueTransitionChecker implements ConstraintChecker {

    private static final Logger log = LoggerFactory.getLogger(VenueTransitionChecker.class);

    private final VenueDistanceServiceImpl distanceService;

    public VenueTransitionChecker(VenueDistanceServiceImpl distanceService) {
        this.distanceService = distanceService;
    }

    @Override
    public String getName() {
        return "Venue Transition > 300m";
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
     * Calculates the penalty for venue transitions greater than 300 meters
     * between consecutive sessions for the same student on the same day.
     * 
     * @param sessions List of all sessions in the schedule
     * @param sessionVenueMap Map of sessions to their assigned venues
     * @return The total penalty for venue transitions exceeding 300 meters
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<Long, Map<String, List<Session>>> byStudentAndDay = new HashMap<>();

        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            byStudentAndDay
                .computeIfAbsent(s.getStudent().getId(), k -> new HashMap<>())
                .computeIfAbsent(s.getDay(), d -> new ArrayList<>())
                .add(s);
        }

        int violations = 0;
        for (Map<String, List<Session>> dayMap : byStudentAndDay.values()) {
            for (List<Session> dailySessions : dayMap.values()) {
                dailySessions.sort(Comparator.comparing(Session::getStartTime));

                for (int i = 0; i < dailySessions.size() - 1; i++) {
                    Session s1 = dailySessions.get(i);
                    Session s2 = dailySessions.get(i + 1);

                    Venue v1 = sessionVenueMap.get(s1);
                    Venue v2 = sessionVenueMap.get(s2);

                    if (v1 == null || v2 == null || v1.getName().equals(v2.getName())) continue;

                    double distance = distanceService.getDistanceScore(v1.getName(), v2.getName());
                    if (distance > 300.0) {
                        log.info("High distance detected: [{}] -> [{}] = {}", v1.getName(), v2.getName(), distance);
                        violations++;
                    }
                }
            }
        }

        return violations;
    }
}
