package com.sunway.course.timetable.evaluator.constraints.soft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 150.0;
    }

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
                        violations++;
                    }
                }
            }
        }

        return violations;
    }
}
