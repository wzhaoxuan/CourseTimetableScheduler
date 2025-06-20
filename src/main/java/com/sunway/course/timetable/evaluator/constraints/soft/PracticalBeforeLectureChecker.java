package com.sunway.course.timetable.evaluator.constraints.soft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;


/**
 * Checks if practical sessions are scheduled before their corresponding lectures.
 * This is a soft constraint, meaning it is preferred but not mandatory.
 * A penalty is applied for each practical session that occurs before its lecture.
 */
public class PracticalBeforeLectureChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Practical Before Lecture";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 5.0;
    }

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<String, List<Session>> sessionsByModule = new HashMap<>();

        for (Session s : sessions) {
            String moduleKey = s.getTypeGroup() != null ? s.getTypeGroup().split("-G")[0] : null;
            if (moduleKey != null) {
                sessionsByModule.computeIfAbsent(moduleKey, k -> new ArrayList<>()).add(s);
            }
        }

        int count = 0;
        for (List<Session> moduleSessions : sessionsByModule.values()) {
            Session lecture = moduleSessions.stream()
                .filter(s -> s.getType().equalsIgnoreCase("Lecture"))
                .findFirst().orElse(null);

            if (lecture == null) continue;

            for (Session s : moduleSessions) {
                if (!s.getType().equalsIgnoreCase("Practical") &&
                    !s.getType().equalsIgnoreCase("Tutorial") &&
                    !s.getType().equalsIgnoreCase("Workshop")) continue;

                if (s.getDay().compareTo(lecture.getDay()) < 0 ||
                    (s.getDay().equals(lecture.getDay()) && s.getStartTime().isBefore(lecture.getEndTime()))) {
                    count++;
                }
            }
        }

        return count;
    }
}
