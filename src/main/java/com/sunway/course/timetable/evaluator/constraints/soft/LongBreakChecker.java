package com.sunway.course.timetable.evaluator.constraints.soft;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

import java.time.Duration;
import java.util.*;

public class LongBreakChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Breaks > 2 hours";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 200.0;
    }

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<Long, Map<String, List<Session>>> grouped = new HashMap<>();

        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            grouped.computeIfAbsent(s.getStudent().getId(), k -> new HashMap<>())
                   .computeIfAbsent(s.getDay(), d -> new ArrayList<>()).add(s);
        }

        int longGaps = 0;
        for (Map<String, List<Session>> dailyMap : grouped.values()) {
            for (List<Session> daySessions : dailyMap.values()) {
                daySessions.sort(Comparator.comparing(Session::getStartTime));

                for (int i = 0; i < daySessions.size() - 1; i++) {
                    Session current = daySessions.get(i);
                    Session next = daySessions.get(i + 1);
                    long minutesGap = Duration.between(current.getEndTime(), next.getStartTime()).toMinutes();

                    if (minutesGap > 120) longGaps++;
                }
            }
        }

        return longGaps;
    }
}
