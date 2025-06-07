package com.sunway.course.timetable.engine;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.engine.AC3DomainPruner.AssignmentOption;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

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
    private final Map<Long, Integer> studentAssignmentCount = new HashMap<>();
    private final Map<Long, Set<String>> studentAssignedTypes = new HashMap<>(); // Map<studentId, Set<module-type>> e.g., "CSC1024-PRACTICAL"

    private final int MIN_GROUP_SIZE = 5;
    private static final int MAX_GROUP_SIZE = 35;


    public BacktrackingScheduler(List<SessionGroupMetaData> sessions,
                                  LecturerAvailabilityMatrix lecturerMatrix,
                                  VenueAvailabilityMatrix venueMatrix,
                                  StudentAvailabilityMatrix studentMatrix,
                                  List<Venue> venues,
                                  VenueDistanceServiceImpl venueDistanceService) {
        this.lecturerMatrix = lecturerMatrix;
        this.venueMatrix = venueMatrix;
        this.studentMatrix = studentMatrix;
        this.sessions = sessions;
        this.venueDistanceService = venueDistanceService;
        this.domains = new HashMap<>();
        this.assignment = new HashMap<>();
        this.studentAssignments = new HashMap<>();

        for (SessionGroupMetaData meta : sessions) {
            List<AssignmentOption> pruned = AC3DomainPruner.pruneDomain(
                lecturerMatrix, venueMatrix, studentMatrix, venues, meta, meta.getEligibleStudents()
            );

            if (pruned.isEmpty()) {
                System.out.printf("[BACKTRACK] No valid options for: %s%n", meta.getTypeGroup());
            }

            String referenceVenue = venues.get(0).getName();
            int requiredCapacity = meta.getTotalStudents();

            pruned.sort(Comparator
                .comparingInt((AssignmentOption opt) -> {
                    int surplus = opt.venue().getCapacity() - requiredCapacity;
                    return (surplus < 0) ? Integer.MAX_VALUE : surplus; // penalize too-small rooms
                })
                .thenComparingDouble(opt -> venueDistanceService.getDistanceScore(referenceVenue, opt.venue().getName()))
            );

            domains.put(meta, pruned);
        }
    }

    public Map<SessionGroupMetaData, AssignmentOption> solve() {
        if (backtrack(0)) {
            return assignment;
        } else {
            return Collections.emptyMap();
        }
    }

    public Map<SessionGroupMetaData, List<Student>> getStudentAssignments() {
        return studentAssignments;
    }

    private boolean backtrack(int index) {
        if (index == sessions.size()) return true;

        SessionGroupMetaData meta = sessions.get(index);
        List<AssignmentOption> options = domains.getOrDefault(meta, Collections.emptyList());

        for (AssignmentOption option : options) {
            if (isConsistent(meta, option)) {
                assign(meta, option);
                if (backtrack(index + 1)) {
                    return true;
                }
                unassign(meta, option);
            }
        }
        return false;
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

        /**
         * Check if the number of available students meets the minimum group size requirement.
         * Current total students = 42, G2 has only 7 students, therefore MIN_GROUP_SIZE must be less.
         * The MIN_GROUP_SIZE is not effecient, need to be change.
         */
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
    // This means the student is already in a group for this specific type (e.g., Practical) of this module
    return assignedTypes.contains(key);
}

}

