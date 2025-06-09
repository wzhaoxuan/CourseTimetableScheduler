package com.sunway.course.timetable.evaluator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.repository.SatisfactionRepository;

@Component
public class FitnessEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FitnessEvaluator.class); 
    public static final double HARD_VIOLATION_WEIGHT = 500.0;

    private final SatisfactionRepository satisfactionRepository;

    public FitnessEvaluator(SatisfactionRepository satisfactionRepository) {
        this.satisfactionRepository = satisfactionRepository;
    }

    public record WeightedConstraint(String name, double weight, double penalty) {
        public double score() { return weight * penalty; }
    }

    public FitnessResult evaluate(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        double totalPenalty = 0.0;
        double maxPenalty = 0.0;

        List<WeightedConstraint> hardConstraints = new ArrayList<>();
        List<WeightedConstraint> softConstraints = new ArrayList<>();
        



        // Hard constraints
        int studentClashes = checkStudentClashes(sessions);
        int lecturerClashes = checkLecturerClashes(sessions);
        

        hardConstraints.add(new WeightedConstraint("Student Clashes", HARD_VIOLATION_WEIGHT, Math.max(0, studentClashes)));
        hardConstraints.add(new WeightedConstraint("Lecturer Clashes", HARD_VIOLATION_WEIGHT, Math.max(0, lecturerClashes)));

        maxPenalty += HARD_VIOLATION_WEIGHT * (studentClashes + lecturerClashes);

        // Soft constraints
        int idleGaps = checkIdleGaps(sessions);
        int badVenues = checkNonPreferredVenues(sessions, sessionVenueMap);
        int practicalBeforeLecture = checkPracticalBeforeLecture(sessions);
        int longBreaks = checkLongBreaks(sessions);

        softConstraints.add(new WeightedConstraint("Idle Gaps", 150.0, Math.max(0, idleGaps)));
        softConstraints.add(new WeightedConstraint("Non-Preferred Venues", 80.0, Math.max(0, badVenues)));
        softConstraints.add(new WeightedConstraint("Practical Before Lecture", 100.0, Math.max(0, practicalBeforeLecture)));
        softConstraints.add(new WeightedConstraint("Breaks > 2 hours", 200.0, Math.max(0, longBreaks)));

        maxPenalty += 150.0 * sessions.size();
        maxPenalty += 80.0 * sessions.size();
        maxPenalty += 200.0 * sessions.size();
        maxPenalty += 100.0 * sessions.size();

        for (WeightedConstraint hc : hardConstraints) totalPenalty += hc.score();
        for (WeightedConstraint sc : softConstraints) totalPenalty += sc.score();

        double percentage = maxPenalty == 0 ? 100.0 : 100.0 * (1.0 - totalPenalty / maxPenalty);
        percentage = Math.max(0.0, Math.min(100.0, Math.round(percentage * 100.0) / 100.0));

        List<FitnessResult.Violation> hard = hardConstraints.stream()
            .map(c -> new FitnessResult.Violation(c.name(), c.weight(), c.penalty(), c.score()))
            .toList();

        List<FitnessResult.Violation> soft = softConstraints.stream()
            .map(c -> new FitnessResult.Violation(c.name(), c.weight(), c.penalty(), c.score()))
            .toList();

        System.out.println("---- FITNESS DEBUG ----");
        System.out.println("Sessions: " + sessions.size());
        System.out.println("Student clashes: " + studentClashes);
        System.out.println("Lecturer clashes: " + lecturerClashes);
        System.out.println("Idle gaps: " + idleGaps);
        System.out.println("Bad venues: " + badVenues);
        System.out.println("Total penalty: " + totalPenalty);
        System.out.println("Max penalty: " + maxPenalty);
        System.out.println("Final Fitness %: " + percentage);

        int totalViolationCount = (int) Stream.concat(hardConstraints.stream(), softConstraints.stream())
            .mapToDouble(WeightedConstraint::penalty)
            .sum();

        Satisfaction satisfaction = new Satisfaction();
        satisfaction.setScore(percentage);
        satisfaction.setConflict(totalViolationCount);
        satisfactionRepository.save(satisfaction);

        return new FitnessResult(percentage, totalPenalty, maxPenalty, hard, soft);
    }

    private static int checkStudentClashes(List<Session> sessions) {
        int clashes = 0;
        Map<Long, List<Session>> byStudent = new HashMap<>();

        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            byStudent
                .computeIfAbsent(s.getStudent().getId(), k -> new ArrayList<>())
                .add(s);
        }

        for (List<Session> studentSessions : byStudent.values()) {
            // Deduplicate by day + start + end time
            List<Session> distinct = studentSessions.stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                            .comparing(Session::getDay)
                            .thenComparing(Session::getStartTime)
                            .thenComparing(Session::getEndTime)
                            .thenComparing(s -> s.getTypeGroup() != null ? s.getTypeGroup() : "")
                        )
                    ),
                    ArrayList::new
                ));

            clashes += countOverlaps(distinct);
        }

        return clashes;
    }


    private static int checkLecturerClashes(List<Session> sessions) {
        int clashes = 0;
        Map<String, List<Session>> byLecturer = new HashMap<>();

        for (Session s : sessions) {
            if (s.getLecturer() == null) continue;
            byLecturer
                .computeIfAbsent(s.getLecturer().getName(), k -> new ArrayList<>())
                .add(s);
        }

        for (List<Session> lecturerSessions : byLecturer.values()) {
            // Deduplicate by day + start + end time
            List<Session> distinct = lecturerSessions.stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                            .comparing(Session::getDay)
                            .thenComparing(Session::getStartTime)
                            .thenComparing(Session::getEndTime)
                        )
                    ),
                    ArrayList::new
                ));

            clashes += countOverlaps(distinct);
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


    private static int checkPracticalBeforeLecture(List<Session> sessions) {
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

private static int checkLongBreaks(List<Session> sessions) {
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


