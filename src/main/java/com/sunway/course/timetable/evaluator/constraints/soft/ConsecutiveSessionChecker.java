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

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        int lecturerViolations = countConsecutiveViolations(
            "LECTURER", groupByPersonAndDay(sessions, true)
        );
        // int studentViolations = countConsecutiveViolations(
        //     "STUDENT", groupByPersonAndDay(sessions, false)
        // );

        log.info("[ConsecutiveSessionChecker] Lecturer violations: {}", lecturerViolations);
        // log.info("[ConsecutiveSessionChecker] Student violations: {}", studentViolations);

        return lecturerViolations;
    }

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

    private int countConsecutiveViolations(String role, Map<String, Map<String, List<Session>>> scheduleMap) {
        int violations = 0;

        for (Map.Entry<String, Map<String, List<Session>>> personEntry : scheduleMap.entrySet()) {
            String person = personEntry.getKey();
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
                            log.warn("[ConsecutiveSessionViolation - {}] {} has 4+ back-to-back sessions on {} starting with: {}",
                                    role, person, dayEntry.getKey(), dailySessions.get(i - 3).getStartTime());
                            break; // one violation per person per day
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

