package com.sunway.course.timetable.engine;

import com.sunway.course.timetable.engine.AC3DomainPruner;
import com.sunway.course.timetable.engine.AC3DomainPruner.AssignmentOption;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

import java.util.*;

public class BacktrackingScheduler {

    private final LecturerAvailabilityMatrix lecturerMatrix;
    private final VenueAvailabilityMatrix venueMatrix;
    private final StudentAvailabilityMatrix studentMatrix;
    private final List<SessionGroupMetaData> sessions;
    private final Map<SessionGroupMetaData, List<AssignmentOption>> domains;
    private final Map<SessionGroupMetaData, AssignmentOption> assignment;
    private final Map<SessionGroupMetaData, List<Student>> studentAssignments;

    public BacktrackingScheduler(List<SessionGroupMetaData> sessions,
                                  LecturerAvailabilityMatrix lecturerMatrix,
                                  VenueAvailabilityMatrix venueMatrix,
                                  StudentAvailabilityMatrix studentMatrix,
                                  List<Venue> venues) {
        this.lecturerMatrix = lecturerMatrix;
        this.venueMatrix = venueMatrix;
        this.studentMatrix = studentMatrix;
        this.sessions = sessions;
        this.domains = new HashMap<>();
        this.assignment = new HashMap<>();
        this.studentAssignments = new HashMap<>();

        for (SessionGroupMetaData meta : sessions) {
            List<AssignmentOption> pruned = AC3DomainPruner.pruneDomain(
                lecturerMatrix, venueMatrix, studentMatrix, venues, meta, meta.getEligibleStudents()
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
        int end = start + 4; // assuming 2 hours
        int day = option.day();

        if (!lecturerMatrix.isAvailable(meta.getLecturerName(), day, start, end)) return false;
        if (!venueMatrix.isAvailable(option.venue(), start, end, day)) return false;

        long availableStudents = meta.getEligibleStudents().stream()
            .filter(s -> studentMatrix.isAvailable(s.getId(), day, start, end))
            .count();

        return availableStudents >= meta.getTotalStudents();
    }

    private void assign(SessionGroupMetaData meta, AssignmentOption option) {
        int start = option.startSlot();
        int end = start + 4;
        int day = option.day();

        lecturerMatrix.assign(meta.getLecturerName(), day, start, end);
        venueMatrix.assign(option.venue(), start, end, day);

        List<Student> assigned = new ArrayList<>();
        for (Student s : meta.getEligibleStudents()) {
            if (studentMatrix.isAvailable(s.getId(), day, start, end)) {
                studentMatrix.markUnavailable(s.getId(), day, start, end);
                assigned.add(s);
            }
        }

        assignment.put(meta, option);
        studentAssignments.put(meta, assigned);
    }

    private void unassign(SessionGroupMetaData meta, AssignmentOption option) {
        int start = option.startSlot();
        int end = start + 4;
        int day = option.day();

        lecturerMatrix.isAvailable(meta.getLecturerName(), day, start, end);
        venueMatrix.isAvailable(option.venue(), start, end, day);

        List<Student> assigned = studentAssignments.getOrDefault(meta, List.of());
        for (Student s : assigned) {
            studentMatrix.isAvailable(s.getId(), day, start, end);
        }

        assignment.remove(meta);
        studentAssignments.remove(meta);
    }
}


