package com.sunway.course.timetable.evaluator.constraints.soft;
import java.time.LocalTime;
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


public class ConsecutiveSessionChecker implements ConstraintChecker {
    private static final Logger log = LoggerFactory.getLogger(ConsecutiveSessionChecker.class);

    @Override
    public String getName() {
        return "4+ Consecutive Sessions";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 700.0; // Tune weight as needed
    }

    /**
     * Calculates the penalty for students and lecturers having 4 or more consecutive sessions on the same day.
     * This method checks both lecturers and students for consecutive sessions.
     * 
     * @param sessions List of all sessions in the schedule
     * @param sessionVenueMap Map of sessions to their assigned venues (not used here)
     * @return The total penalty for consecutive sessions
     */
    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        int lecturerViolations = countConsecutiveViolations(
            "LECTURER", groupByPersonAndDay(sessions, true)
        ); 

        int studentViolations = countConsecutiveViolations(
            "STUDENT", groupByPersonAndDay(sessions, false)
        );

        return lecturerViolations + studentViolations;
    }

    /**
     * Groups sessions by person (lecturer or student) and day.
     * 
     * @param sessions List of all sessions
     * @param isLecturer True if grouping by lecturer, false for student
     * @return A map where keys are person names/IDs and values are maps of days to lists of sessions
     */
    private Map<String, Map<String, List<Session>>> groupByPersonAndDay(List<Session> sessions, boolean isLecturer) {
        Map<String, Map<String, List<Session>>> grouped = new HashMap<>();
        for (Session s : sessions) {
            String person = isLecturer
                ? (s.getLecturer() != null ? s.getLecturer().getName() : null)
                : (s.getStudent() != null ? String.valueOf(s.getStudent().getId()) : null);
            if (person == null) continue;

            grouped
                .computeIfAbsent(person, k -> new HashMap<>())
                .computeIfAbsent(s.getDay(), k -> new ArrayList<>())
                .add(s);
        }
        return grouped;
    }

    /**
     * Counts the number of violations where a person has 4 or more consecutive sessions on the same day.
     * 
     * @param role The role being checked
     * @param scheduleMap Map of persons to their daily sessions
     * @return The number of violations found
     */
    private int countConsecutiveViolations(String role, Map<String, Map<String, List<Session>>> scheduleMap) {
        int violations = 0;

        for (Map.Entry<String, Map<String, List<Session>>> personEntry : scheduleMap.entrySet()) {
            Map<String, List<Session>> dayMap = personEntry.getValue();

            for (Map.Entry<String, List<Session>> dayEntry : dayMap.entrySet()) {
                List<Session> dailySessions = dayEntry.getValue();
                dailySessions.sort(Comparator.comparing(Session::getStartTime));

                int consecutive = 1;
                for (int i = 1; i < dailySessions.size(); i++) {
                    LocalTime prevEnd = dailySessions.get(i - 1).getEndTime();
                    LocalTime currStart = dailySessions.get(i).getStartTime();

                    if (prevEnd.equals(currStart)) {
                        consecutive++;
                        if (consecutive >= 4) {
                            violations++;
                            break; 
                        }
                    } else {
                        consecutive = 1;
                    }
                }
            }
        }
        return violations;
    }
}

