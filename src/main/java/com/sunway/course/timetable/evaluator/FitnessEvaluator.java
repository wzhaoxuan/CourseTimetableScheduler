package com.sunway.course.timetable.evaluator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

@Component
public class FitnessEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FitnessEvaluator.class); 
    public static final double HARD_VIOLATION_WEIGHT = 1000.0;

    public record WeightedConstraint(String name, double weight, double penalty) {
        public double score() { return weight * penalty; }
    }

    public FitnessResult evaluate(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {

        // for (Session session : sessions) {
        //     log.info("Scheduled session: {} on {} from {} to {} with Students: {} and Lecturer: {}",
        //         session.getType(), session.getDay(),
        //         session.getStartTime(), session.getEndTime(),
        //         session.getStudent(), session.getLecturer().getName());
        // }

        // for(Map.Entry<Session, Venue> entry : sessionVenueMap.entrySet()) {
        //     Session session = entry.getKey();
        //     Venue venue = entry.getValue();
        //     log.info("Session {} assigned to venue {}", session, venue.getName());
        // }

        double totalPenalty = 0.0;
        double maxPenalty = 0.0;

        List<WeightedConstraint> hardConstraints = new ArrayList<>();
        List<WeightedConstraint> softConstraints = new ArrayList<>();

        // Hard constraints
        int studentClashes = checkStudentClashes(sessions);
        int lecturerClashes = checkLecturerClashes(sessions);
        hardConstraints.add(new WeightedConstraint("Student Clashes", HARD_VIOLATION_WEIGHT, studentClashes));
        hardConstraints.add(new WeightedConstraint("Lecturer Clashes", HARD_VIOLATION_WEIGHT, lecturerClashes));
        maxPenalty += HARD_VIOLATION_WEIGHT * sessions.size();
        maxPenalty += HARD_VIOLATION_WEIGHT * sessions.size();

        // Soft constraints
        int idleGaps = checkIdleGaps(sessions);
        int badVenues = checkNonPreferredVenues(sessions, sessionVenueMap);
        softConstraints.add(new WeightedConstraint("Idle Gaps", 2.0, idleGaps));
        softConstraints.add(new WeightedConstraint("Non-Preferred Venues", 1.5, badVenues));
        maxPenalty += 2.0 * sessions.size();
        maxPenalty += 1.5 * sessions.size();

        for (WeightedConstraint hc : hardConstraints) totalPenalty += hc.score();
        for (WeightedConstraint sc : softConstraints) totalPenalty += sc.score();

        double percentage = maxPenalty == 0 ? 100.0 : Math.max(0.0, 100.0 * (1.0 - totalPenalty / maxPenalty));
        percentage = Math.round(percentage * 100.0) / 100.0;

        List<FitnessResult.Violation> hard = hardConstraints.stream()
            .map(c -> new FitnessResult.Violation(c.name(), c.weight(), c.penalty(), c.score()))
            .toList();

        List<FitnessResult.Violation> soft = softConstraints.stream()
            .map(c -> new FitnessResult.Violation(c.name(), c.weight(), c.penalty(), c.score()))
            .toList();

        return new FitnessResult(percentage, totalPenalty, maxPenalty, hard, soft);
    }

    private static int checkStudentClashes(List<Session> sessions) {
        int clashes = 0;
        Map<Long, List<Session>> byStudent = new HashMap<>();
        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            byStudent.computeIfAbsent(s.getStudent().getId(), k -> new ArrayList<>()).add(s);
        }
        for (List<Session> studentSessions : byStudent.values()) {
            clashes += countOverlaps(studentSessions);
        }
        return clashes;
    }

    private static int checkLecturerClashes(List<Session> sessions) {
        int clashes = 0;
        Map<String, List<Session>> byLecturer = new HashMap<>();
        for (Session s : sessions) {
            if (s.getLecturer() == null) continue;
            byLecturer.computeIfAbsent(s.getLecturer().getName(), k -> new ArrayList<>()).add(s);
        }
        for (List<Session> lecturerSessions : byLecturer.values()) {
            clashes += countOverlaps(lecturerSessions);
        }
        return clashes;
    }

    private static int countOverlaps(List<Session> list) {
        int overlaps = 0;
        list.sort(Comparator.comparing(Session::getDay).thenComparing(Session::getStartTime));
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Session a = list.get(i);
                Session b = list.get(j);
                if (!a.getDay().equals(b.getDay())) break;
                if (a.getEndTime().isAfter(b.getStartTime())) overlaps++;
            }
        }
        return overlaps;
    }

    private static int checkIdleGaps(List<Session> sessions) {
        Map<Long, Map<String, List<Session>>> grouped = new HashMap<>();
        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            grouped.computeIfAbsent(s.getStudent().getId(), k -> new HashMap<>())
                   .computeIfAbsent(s.getDay(), d -> new ArrayList<>()).add(s);
        }

        int gaps = 0;
        for (Map<String, List<Session>> dailyMap : grouped.values()) {
            for (List<Session> daySessions : dailyMap.values()) {
                daySessions.sort(Comparator.comparing(Session::getStartTime));
                for (int i = 0; i < daySessions.size() - 1; i++) {
                    if (daySessions.get(i).getEndTime().isBefore(daySessions.get(i+1).getStartTime())) {
                        gaps++;
                    }
                }
            }
        }
        return gaps;
    }

    private static int checkNonPreferredVenues(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        int nonPreferred = 0;
        for (Session s : sessions) {
            Venue venue = sessionVenueMap.get(s);
            if (venue != null && venue.getName().toLowerCase().contains("unknown")) {
                nonPreferred++;
            }
        }
        return nonPreferred;
    }
}


