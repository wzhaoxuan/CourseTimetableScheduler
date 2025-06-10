package com.sunway.course.timetable.evaluator;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;

@Component
public class FitnessEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FitnessEvaluator.class); 
    public static final double HARD_VIOLATION_WEIGHT = 500.0;

    private final SatisfactionRepository satisfactionRepository;
    private final VenueDistanceServiceImpl venueDistanceService;

    public FitnessEvaluator(SatisfactionRepository satisfactionRepository, 
                            VenueDistanceServiceImpl venueDistanceService) {
        this.satisfactionRepository = satisfactionRepository;
        this.venueDistanceService = venueDistanceService;
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
        int moduleClashes = checkModuleClash(sessions);
        int invalidDays = checkInvalidDays(sessions);
        int overCapacity = checkVenueOverCapacity(sessions, sessionVenueMap);
        int duplicateTypes = checkDuplicateTypeGroupPerWeek(sessions);

        hardConstraints.add(new WeightedConstraint("Student Clashes", HARD_VIOLATION_WEIGHT, Math.max(0, studentClashes)));
        hardConstraints.add(new WeightedConstraint("Lecturer Clashes", HARD_VIOLATION_WEIGHT, Math.max(0, lecturerClashes)));
        hardConstraints.add(new WeightedConstraint("Module Clashes", HARD_VIOLATION_WEIGHT, Math.max(0, moduleClashes)));
        hardConstraints.add(new WeightedConstraint("Sessions on Invalid Days", HARD_VIOLATION_WEIGHT, invalidDays));
        hardConstraints.add(new WeightedConstraint("Venue Over Capacity", HARD_VIOLATION_WEIGHT, overCapacity));
        hardConstraints.add(new WeightedConstraint("Duplicate Session Types Per Module", HARD_VIOLATION_WEIGHT, duplicateTypes));
        
        maxPenalty += HARD_VIOLATION_WEIGHT * (studentClashes + lecturerClashes + moduleClashes + invalidDays + overCapacity + duplicateTypes);

        // Soft constraints

        int badVenues = checkNonPreferredVenues(sessions, sessionVenueMap);
        // int practicalBeforeLecture = checkPracticalBeforeLecture(sessions);
        // int longBreaks = checkLongBreaks(sessions);
        int lateSessions = checkUnpreferredTimeOfDay(sessions);
        int venueTransitions = checkVenueTransitionDistance(sessions, sessionVenueMap, venueDistanceService);

        softConstraints.add(new WeightedConstraint("Late Sessions", 50.0, Math.max(0, lateSessions)));
        softConstraints.add(new WeightedConstraint("Venue Transition > 500m", 150.0, venueTransitions));
        softConstraints.add(new WeightedConstraint("Non-Preferred Venues", 80.0, Math.max(0, badVenues)));
        // softConstraints.add(new WeightedConstraint("Practical Before Lecture", 100.0, Math.max(0, practicalBeforeLecture)));
        // softConstraints.add(new WeightedConstraint("Breaks > 2 hours", 200.0, Math.max(0, longBreaks)));

        maxPenalty += 50.0 * sessions.size();
        maxPenalty += 80.0 * sessions.size();
        maxPenalty += 150.0 * sessions.size();
        
        // maxPenalty += 200.0 * sessions.size();
        // maxPenalty += 100.0 * sessions.size();

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
        System.out.println("Module clashes: " + moduleClashes);
        System.out.println("Invalid days: " + invalidDays);
        System.out.println("Venue over capacity: " + overCapacity);
        System.out.println("Duplicate types per module: " + duplicateTypes);
        System.out.println("Late sessions: " + lateSessions);
        System.out.println("Venue transitions > 500m: " + venueTransitions);
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

    private static int checkModuleClash(List<Session> sessions) {
        Map<Long, List<Session>> sessionsByStudent = new HashMap<>();

        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            long studentId = s.getStudent().getId();
            sessionsByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(s);
        }

        int clashCount = 0;

        for (List<Session> studentSessions : sessionsByStudent.values()) {
            studentSessions.sort(Comparator
                .comparing(Session::getDay)
                .thenComparing(Session::getStartTime));

            for (int i = 0; i < studentSessions.size(); i++) {
                Session a = studentSessions.get(i);
                String moduleA = extractModuleIdFromTypeGroup(a.getTypeGroup());
                String typeGroupA = a.getTypeGroup();

                for (int j = i + 1; j < studentSessions.size(); j++) {
                    Session b = studentSessions.get(j);
                    String moduleB = extractModuleIdFromTypeGroup(b.getTypeGroup());
                    String typeGroupB = b.getTypeGroup();

                    if (!a.getDay().equals(b.getDay())) break;
                    if (!moduleA.equals(moduleB)) continue;

                    // Same module but different type-group, check overlap
                    if (!typeGroupA.equals(typeGroupB) &&
                        a.getEndTime().isAfter(b.getStartTime())) {
                        clashCount++;
                    }
                }
            }
        }

        return clashCount;
    }


    private static int checkInvalidDays(List<Session> sessions) {
        Set<String> validDays = Set.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        return (int) sessions.stream()
            .filter(s -> !validDays.contains(s.getDay()))
            .count();
    }

    private static int checkVenueOverCapacity(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        Map<String, Integer> sessionGroupSize = new HashMap<>();

        for (Session s : sessions) {
            String key = s.getTypeGroup() + "-" + s.getDay() + "-" + s.getStartTime();
            sessionGroupSize.put(key, sessionGroupSize.getOrDefault(key, 0) + 1);
        }

        int overCap = 0;
        for (Session s : sessions) {
            Venue v = sessionVenueMap.get(s);
            if (v == null) continue;

            String key = s.getTypeGroup() + "-" + s.getDay() + "-" + s.getStartTime();
            int groupSize = sessionGroupSize.getOrDefault(key, 0);

            String sessionType = s.getType().toLowerCase();
            String venueType = v.getType() != null ? v.getType().toLowerCase() : "";

            boolean isLecture = sessionType.contains("lecture");
            boolean isPracticalTutorialWorkshop =
                sessionType.contains("practical") ||
                sessionType.contains("tutorial") ||
                sessionType.contains("workshop");

            boolean lectureAllowedVenue =
                isLecture && (venueType.contains("auditorium") || venueType.contains("lecture theatre"));

            boolean practicalAllowedVenue =
                isPracticalTutorialWorkshop && (venueType.contains("room") || venueType.contains("lab"));

            if (groupSize > v.getCapacity() && !(lectureAllowedVenue || practicalAllowedVenue)) {
                overCap++;
            }
        }

        return overCap;
    }

    private static int checkDuplicateTypeGroupPerWeek(List<Session> sessions) {
        Map<String, Set<String>> typeGroupScheduleMap = new HashMap<>();

        for (Session s : sessions) {
            String typeGroup = s.getTypeGroup();
            if (typeGroup == null) continue;

            String slotKey = s.getDay() + "-" + s.getStartTime();
            typeGroupScheduleMap
                .computeIfAbsent(typeGroup, k -> new HashSet<>())
                .add(slotKey);
        }

        int violations = 0;
        for (Set<String> times : typeGroupScheduleMap.values()) {
            if (times.size() > 1) {
                violations += times.size() - 1; // count extra occurrences
            }
        }

        return violations;
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

    private static int checkUnpreferredTimeOfDay(List<Session> sessions) {
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

    private static int checkVenueTransitionDistance(List<Session> sessions, Map<Session, Venue> sessionVenueMap, VenueDistanceServiceImpl distanceService) {
        Map<Long, Map<String, List<Session>>> byStudentAndDay = new HashMap<>();

        // 1. Group sessions by student → day
        for (Session s : sessions) {
            if (s.getStudent() == null) continue;
            byStudentAndDay
                .computeIfAbsent(s.getStudent().getId(), k -> new HashMap<>())
                .computeIfAbsent(s.getDay(), d -> new ArrayList<>())
                .add(s);
        }

        int violations = 0;

        for (Map<String, List<Session>> dayMap : byStudentAndDay.values()) {
            for (List<Session> dailySessions : dayMap.values()) {
                dailySessions.sort(Comparator.comparing(Session::getStartTime));

                for (int i = 0; i < dailySessions.size() - 1; i++) {
                    Session s1 = dailySessions.get(i);
                    Session s2 = dailySessions.get(i + 1);

                    Venue v1 = sessionVenueMap.get(s1);
                    Venue v2 = sessionVenueMap.get(s2);

                    if (v1 == null || v2 == null || v1.getName().equals(v2.getName())) continue;

                    double distance = distanceService.getDistanceScore(v1.getName(), v2.getName());

                    if (distance > 300.0) {
                        violations++;
                    }
                }
            }
        }

        return violations;
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

    private static String extractModuleIdFromTypeGroup(String typeGroup) {
        if (typeGroup == null || !typeGroup.contains("-")) return null;
        return typeGroup.split("-")[0];
    }

    // Add utility to convert weekday name to index
    private static int getDayIndex(String day) {
        return switch (day.toLowerCase()) {
            case "monday" -> 0;
            case "tuesday" -> 1;
            case "wednesday" -> 2;
            case "thursday" -> 3;
            case "friday" -> 4;
            default -> 5; // Invalid or weekend
        };
    }

}


