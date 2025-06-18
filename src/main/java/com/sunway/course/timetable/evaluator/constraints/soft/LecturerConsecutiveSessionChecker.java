package com.sunway.course.timetable.evaluator.constraints.soft;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.evaluator.ConstraintChecker;
import com.sunway.course.timetable.evaluator.ConstraintType;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;


public class LecturerConsecutiveSessionChecker implements ConstraintChecker {

    @Override
    public String getName() {
        return "Lecturer 4+ Consecutive Sessions";
    }

    @Override
    public ConstraintType getType() {
        return ConstraintType.SOFT;
    }

    @Override
    public double getWeight() {
        return 1000.0; // Tune weight as needed
    }

    @Override
    public double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<String, Map<String, List<Session>>> sessionsByLecturerAndDay = new HashMap<>();

        for (Session s : sessions) {
            if (s.getLecturer() == null) continue;

            String lecturer = s.getLecturer().getName();
            String day = s.getDay();

            sessionsByLecturerAndDay
                .computeIfAbsent(lecturer, k -> new HashMap<>())
                .computeIfAbsent(day, k -> new ArrayList<>())
                .add(s);
        }

        int violations = 0;

        for (Map<String, List<Session>> dailySessions : sessionsByLecturerAndDay.values()) {
            for (List<Session> list : dailySessions.values()) {
                list.sort(Comparator.comparing(Session::getStartTime));
                
                int consecutive = 1;
                for (int i = 1; i < list.size(); i++) {
                    LocalTime prevEnd = list.get(i - 1).getEndTime();
                    LocalTime currStart = list.get(i).getStartTime();

                    if (prevEnd.equals(currStart)) {
                        consecutive++;
                        if (consecutive >= 4) {
                            violations++;
                            break; // Count one violation per day only
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

