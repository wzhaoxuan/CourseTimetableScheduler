package com.sunway.course.timetable.engine;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.engine.DomainPruner.AssignmentOption;
import com.sunway.course.timetable.evaluator.FitnessEvaluator;
import com.sunway.course.timetable.evaluator.FitnessResult;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;

public class BacktrackingScheduler {
    private static final Logger log = LoggerFactory.getLogger(BacktrackingScheduler.class);

    private final LecturerAvailabilityMatrix lecturerMatrix;
    private final VenueAvailabilityMatrix venueMatrix;
    private final StudentAvailabilityMatrix studentMatrix;
    private final List<SessionGroupMetaData> sessions;
    private final Map<SessionGroupMetaData, List<AssignmentOption>> domains;
    private final Map<SessionGroupMetaData, AssignmentOption> assignment;
    private final Map<SessionGroupMetaData, List<Student>> studentAssignments;
    private final VenueDistanceServiceImpl venueDistanceService;
    public final LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil;
    public final LecturerServiceImpl lecturerService;
    private final Map<Long, Integer> studentAssignmentCount = new HashMap<>();
    private final Map<Long, Set<String>> studentAssignedTypes = new HashMap<>();
    private final FitnessEvaluator fitnessEvaluator;
    private double bestFitnessScore = -1;
    private Map<SessionGroupMetaData, AssignmentOption> bestAssignment = new HashMap<>();
    private Map<SessionGroupMetaData, List<Student>> bestStudentAssignments = new HashMap<>();

    private final int MIN_GROUP_SIZE = 5;
    private static final int MAX_GROUP_SIZE = 35;

    public BacktrackingScheduler(List<SessionGroupMetaData> sessions,
                                  LecturerAvailabilityMatrix lecturerMatrix,
                                  VenueAvailabilityMatrix venueMatrix,
                                  StudentAvailabilityMatrix studentMatrix,
                                  List<Venue> venues,
                                  VenueDistanceServiceImpl venueDistanceService,
                                  LecturerServiceImpl lecturerService,
                                  LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil,
                                  FitnessEvaluator fitnessEvaluator
                                  ) {
        this.lecturerMatrix = lecturerMatrix;
        this.venueMatrix = venueMatrix;
        this.studentMatrix = studentMatrix;
        this.sessions = sessions;
        this.venueDistanceService = venueDistanceService;
        this.lecturerService = lecturerService;
        this.lecturerDayAvailabilityUtil = lecturerDayAvailabilityUtil;
        this.fitnessEvaluator = fitnessEvaluator;
        this.assignment = new HashMap<>();
        this.studentAssignments = new HashMap<>();

        this.domains = new HashMap<>(AC3ConstraintPropagator.propagate(
            sessions,
            lecturerMatrix,
            venueMatrix,
            studentMatrix,
            venues,
            lecturerService,
            lecturerDayAvailabilityUtil
        ));

        for (SessionGroupMetaData meta : sessions) {
            List<AssignmentOption> pruned = this.domains.getOrDefault(meta, new ArrayList<>());

            if (pruned.isEmpty()) {
                System.out.printf("[BACKTRACK] No valid options for: %s%n", meta.getTypeGroup());
            }

            String referenceVenue = venues.get(0).getName();
            int requiredCapacity = meta.getTotalStudents();

            pruned.sort(Comparator
                .comparingInt(AssignmentOption::day)
                .thenComparingInt(AssignmentOption::startSlot)
                .thenComparingInt((AssignmentOption opt) -> {
                    int surplus = opt.venue().getCapacity() - requiredCapacity;
                    return (surplus < 0) ? Integer.MAX_VALUE : surplus;
                })
                .thenComparingDouble(opt -> venueDistanceService.getDistanceScore(referenceVenue, opt.venue().getName()))
            );

            domains.put(meta, pruned);
        }
    }

    public Map<SessionGroupMetaData, AssignmentOption> solve() {
        backtrack(0);
        assignment.clear();
        assignment.putAll(bestAssignment);
        studentAssignments.clear();
        studentAssignments.putAll(bestStudentAssignments);
        return assignment;
    }

    public Map<SessionGroupMetaData, List<Student>> getStudentAssignments() {
        return studentAssignments;
    }

    private boolean backtrack(int index) {
        if (index == sessions.size()) {
            if (assignment.size() < sessions.size()) {
                // Not a complete assignment, skip this partial solution
                return false;
            }
            
            List<Session> currentSessions = buildCurrentSessions();
            Map<Session, Venue> venueMap = buildCurrentVenueMap();
            FitnessResult result = fitnessEvaluator.evaluate(currentSessions, venueMap);

            if (result.getPercentage() > bestFitnessScore) {
                bestFitnessScore = result.getPercentage();
                bestAssignment.clear();
                bestAssignment.putAll(assignment);
                bestStudentAssignments.clear();
                bestStudentAssignments.putAll(studentAssignments);
            }
            return false; // Continue searching
        }

        SessionGroupMetaData meta = sessions.get(index);
        List<AssignmentOption> options = domains.getOrDefault(meta, List.of());

        for (AssignmentOption option : options) {
            if (isConsistent(meta, option)) {
                assign(meta, option);
                backtrack(index + 1);
                unassign(meta, option);
            }
        }
        return false;
    }

    private List<Session> buildCurrentSessions() {
        List<Session> sessions = new ArrayList<>();
        for (Map.Entry<SessionGroupMetaData, AssignmentOption> entry : assignment.entrySet()) {
            SessionGroupMetaData meta = entry.getKey();
            AssignmentOption option = entry.getValue();
            List<Student> assignedStudents = studentAssignments.getOrDefault(meta, List.of());
            for (Student student : assignedStudents) {
                Session session = new Session();
                session.setType(meta.getType());
                session.setTypeGroup(meta.getTypeGroup());
                session.setLecturer(lecturerService.getLecturerByName(meta.getLecturerName()).orElse(null));
                session.setDay(getDayName(option.day()));
                session.setStartTime(LocalTime.of(8, 0).plusMinutes(option.startSlot() * 30L));
                session.setEndTime(LocalTime.of(8, 0).plusMinutes((option.startSlot() + 4) * 30L));
                session.setStudent(student);
                sessions.add(session);
            }
        }
        return sessions;
    }

    private Map<Session, Venue> buildCurrentVenueMap() {
        Map<Session, Venue> map = new HashMap<>();
        for (Map.Entry<SessionGroupMetaData, AssignmentOption> entry : assignment.entrySet()) {
            SessionGroupMetaData meta = entry.getKey();
            AssignmentOption option = entry.getValue();
            List<Student> assignedStudents = studentAssignments.getOrDefault(meta, List.of());
            for (Student student : assignedStudents) {
                Session session = new Session();
                session.setType(meta.getType());
                session.setTypeGroup(meta.getTypeGroup());
                session.setLecturer(lecturerService.getLecturerByName(meta.getLecturerName()).orElse(null));
                session.setDay(getDayName(option.day()));
                session.setStartTime(LocalTime.of(8, 0).plusMinutes(option.startSlot() * 30L));
                session.setEndTime(LocalTime.of(8, 0).plusMinutes((option.startSlot() + 4) * 30L));
                session.setStudent(student);
                map.put(session, option.venue());
            }
        }
        return map;
    }

    private String getDayName(int index) {
        return switch (index) {
            case 0 -> "Monday";
            case 1 -> "Tuesday";
            case 2 -> "Wednesday";
            case 3 -> "Thursday";
            case 4 -> "Friday";
            default -> throw new IllegalArgumentException("Invalid day index: " + index);
        };
    }

    private boolean isConsistent(SessionGroupMetaData meta, AssignmentOption option) {
        int start = option.startSlot();
        int end = start + 4;
        int day = option.day();

        if (!lecturerMatrix.isAvailable(meta.getLecturerName(), day, start, end)) return false;
        if (!venueMatrix.isAvailable(option.venue(), start, end, day)) return false;

        long availableStudents = meta.getEligibleStudents().stream()
            .filter(s -> studentMatrix.isAvailable(s.getId(), day, start, end))
            .filter(s -> !isAssignedToSameTypeGroup(meta, s.getId()))
            .count();

        return availableStudents >= MIN_GROUP_SIZE;
    }

    private void assign(SessionGroupMetaData meta, AssignmentOption option) {
        int start = option.startSlot();
        int end = start + 4;
        int day = option.day();

        lecturerMatrix.assign(meta.getLecturerName(), day, start, end);
        venueMatrix.assign(option.venue(), start, end, day);

        List<Student> assigned = meta.getEligibleStudents().stream()
            .filter(s -> studentMatrix.isAvailable(s.getId(), day, start, end))
            .filter(s -> !isAssignedToSameTypeGroup(meta, s.getId()))
            .sorted(Comparator.comparingInt(s -> studentAssignmentCount.getOrDefault(s.getId(), 0)))
            .limit(MAX_GROUP_SIZE)
            .collect(Collectors.toList());

        for (Student s : assigned) {
            studentMatrix.markUnavailable(s.getId(), day, start, end);
            String key = meta.getModuleId() + "-" + meta.getType().toUpperCase();
            studentAssignedTypes.computeIfAbsent(s.getId(), k -> new HashSet<>()).add(key);
            studentAssignmentCount.put(s.getId(), studentAssignmentCount.getOrDefault(s.getId(), 0) + 1);
        }

        assignment.put(meta, option);
        studentAssignments.put(meta, assigned);
    }

    private void unassign(SessionGroupMetaData meta, AssignmentOption option) {
        int start = option.startSlot();
        int end = start + 4;
        int day = option.day();

        List<Student> assigned = studentAssignments.getOrDefault(meta, List.of());
        for (Student s : assigned) {
            studentMatrix.markAvailable(s.getId(), day, start, end);
            String key = meta.getModuleId() + "-" + meta.getType().toUpperCase();
            Set<String> types = studentAssignedTypes.get(s.getId());
            if (types != null) {
                types.remove(key);
                if (types.isEmpty()) studentAssignedTypes.remove(s.getId());
            }
            studentAssignmentCount.put(s.getId(), studentAssignmentCount.getOrDefault(s.getId(), 1) - 1);
        }

        assignment.remove(meta);
        studentAssignments.remove(meta);
    }

    private boolean isAssignedToSameTypeGroup(SessionGroupMetaData meta, long studentId) {
        String key = meta.getModuleId() + "-" + meta.getType().toUpperCase();
        Set<String> assignedTypes = studentAssignedTypes.getOrDefault(studentId, Set.of());
        return assignedTypes.contains(key);
    }
}


