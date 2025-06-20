package com.sunway.course.timetable.evaluator.constraints.soft;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

/**
 * Checks if students and lecturers have only one session scheduled per day.
 * This is a soft constraint, meaning it can be violated but incurs a penalty.
 */
public class OneSessionDayChecker implements ConstraintChecker {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OneSessionDayChecker.class);

    @Override
    public String getName() {
        return "Single Session Day (Student)";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 30.0; // Adjust based on how important this is

    }

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<Long, Map<String, Set<String>>> studentDaySlots = new HashMap<>();
        Map<String, Map<String, Set<String>>> lecturerDaySlots = new HashMap<>();

        for (Session s : sessions) {
            String timeSlotKey = s.getStartTime() + "-" + s.getEndTime() + "-" + s.getTypeGroup();
            String day = s.getDay();

            if (s.getStudent() != null) {
                long sid = s.getStudent().getId();
                studentDaySlots
                    .computeIfAbsent(sid, k -> new HashMap<>())
                    .computeIfAbsent(day, d -> new HashSet<>())
                    .add(timeSlotKey);
            }

            if (s.getLecturer() != null) {
                String lid = s.getLecturer().getName(); 
                lecturerDaySlots
                    .computeIfAbsent(lid, k -> new HashMap<>())
                    .computeIfAbsent(day, d -> new HashSet<>())
                    .add(timeSlotKey);
            }
        }

        int penalty = 0;
        for (Map<String, Set<String>> perDay : studentDaySlots.values()) {
            // log.info("Student day slots: {}", perDay);
            penalty += perDay.values().stream().filter(set -> set.size() == 1).count();
        }
        for (Map<String, Set<String>> perDay : lecturerDaySlots.values()) {
            // log.info("Lecturer day slots: {}", perDay);
            penalty += perDay.values().stream().filter(set -> set.size() == 1).count();
        }

        return penalty;
    }

}

