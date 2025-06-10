package com.sunway.course.timetable.evaluator.constraints.soft;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

public class LateSessionChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Late Sessions";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 50.0;
    }

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Set<String> seen = new HashSet<>();
        int penalty = 0;

        for (Session s : sessions) {
            String key = s.getTypeGroup() + "-" + s.getDay() + "-" + s.getStartTime();
            if (seen.add(key)) {
                LocalTime start = s.getStartTime();
                if (!start.isBefore(LocalTime.of(16, 0))) {  // 4:00 PM or later
                    penalty++;
                }
            }
        }
        return penalty;
    }
}
