package com.sunway.course.timetable.evaluator.constraints.soft;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(PracticalBeforeLectureChecker.class);

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
        return 80.0;
    }

    /**
     * Calculates the penalty for practical sessions scheduled before their corresponding lectures.
     * This method checks each module's sessions to see if any practical session occurs before its lecture.
     *
     * @param sessions List of all sessions in the schedule
     * @param sessionVenueMap Map of sessions to their assigned venues (not used here)
     * @return The total penalty for practical sessions scheduled before lectures
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<String, List<Session>> sessionsByModule = new HashMap<>();

        for (Session s : sessions) {
            String typeGroup = s.getTypeGroup();
            if (typeGroup == null || !typeGroup.contains("-")) continue;

            String moduleId = typeGroup.split("-")[0];
            sessionsByModule.computeIfAbsent(moduleId, k -> new ArrayList<>()).add(s);
        }

        int violations = 0;

        for (Map.Entry<String, List<Session>> entry : sessionsByModule.entrySet()) {
            List<Session> moduleSessions = entry.getValue();

            // Find the lecture session
            Session lecture = moduleSessions.stream()
                .filter(s -> s.getType().equalsIgnoreCase("Lecture"))
                .findFirst()
                .orElse(null);

            if (lecture == null) continue;

            int lectureDay = dayToIndex(lecture.getDay());
            LocalTime lectureTime = lecture.getStartTime();

            boolean hasEarlySession = moduleSessions.stream()
                .filter(s -> {
                    String type = s.getType();
                    return type.equalsIgnoreCase("Practical") ||
                           type.equalsIgnoreCase("Tutorial") ||
                           type.equalsIgnoreCase("Workshop");
                })
                .anyMatch(s -> {
                    int sessionDay = dayToIndex(s.getDay());
                    LocalTime sessionTime = s.getStartTime();
                    return sessionDay < lectureDay ||
                           (sessionDay == lectureDay && sessionTime.isBefore(lectureTime));
                });

            if (hasEarlySession) {
                violations++;
            }
        }

        return violations;
    }

    /**
     * Converts a day string to an index for comparison.
     * 
     * @param day The day of the week as a string (e.g., "Monday", "Tuesday")
     * @return An integer index representing the day of the week (0 for Monday, 1 for Tuesday, etc.)
     */
    private int dayToIndex(String day) {
        return switch (day.toLowerCase()) {
            case "monday" -> 0;
            case "tuesday" -> 1;
            case "wednesday" -> 2;
            case "thursday" -> 3;
            case "friday" -> 4;
            default -> 5;
        };
    }
}
