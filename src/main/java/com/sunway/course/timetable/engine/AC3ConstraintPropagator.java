package com.sunway.course.timetable.engine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.engine.DomainPruner.AssignmentOption;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;

public class AC3ConstraintPropagator {

    private static final Logger log = LoggerFactory.getLogger(AC3ConstraintPropagator.class);

    public static class AC3Result {
        public final Map<SessionGroupMetaData, List<AssignmentOption>> domains;
        public final Map<SessionGroupMetaData, List<DomainRejectionReason>> rejectionLogs;

        public AC3Result(Map<SessionGroupMetaData, List<AssignmentOption>> domains,
                         Map<SessionGroupMetaData, List<DomainRejectionReason>> rejectionLogs) {
            this.domains = domains;
            this.rejectionLogs = rejectionLogs;
        }
    }

    public static AC3Result propagate(
        List<SessionGroupMetaData> failedSessionGroups,
        LecturerAvailabilityMatrix lecturerMatrix,
        VenueAvailabilityMatrix venueMatrix,
        StudentAvailabilityMatrix studentMatrix,
        List<Venue> allVenues,
        LecturerServiceImpl lecturerService,
        LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil
    ) {
        Map<SessionGroupMetaData, List<AssignmentOption>> domains = new HashMap<>();
        Map<SessionGroupMetaData, List<DomainRejectionReason>> rejectionLogs = new HashMap<>();
        Map<SessionGroupMetaData, Set<SessionGroupMetaData>> neighbors = new HashMap<>();
        
        // 1. For each SessionGroupMetaData, call AC3DomainPruner.pruneDomain(...) to initialize the domain
        for (SessionGroupMetaData group : failedSessionGroups) {
            List<DomainRejectionReason> rejectionList = new ArrayList<>();
            List<AssignmentOption> domain = DomainPruner.pruneDomain(
                lecturerMatrix, venueMatrix, studentMatrix, allVenues,
                group, group.getEligibleStudents(), lecturerService, lecturerDayAvailabilityUtil,
                rejectionList
            );

            domain.sort(Comparator
                .comparingInt(AssignmentOption::day)
                .thenComparingInt(AssignmentOption::startSlot)
                .thenComparingInt(opt -> opt.venue().getCapacity()));
            domains.put(group, domain);
            rejectionLogs.put(group, rejectionList);
        }

        // 2. Build neighbors (arcs) between groups that share lecturers or students
        for (SessionGroupMetaData g1 : failedSessionGroups) {
            for (SessionGroupMetaData g2 : failedSessionGroups) {
                if (!g1.equals(g2)) {
                    boolean sameLecturer = g1.getLecturerName().equalsIgnoreCase(g2.getLecturerName());
                    boolean sharedStudents = !Collections.disjoint(
                        g1.getEligibleStudents().stream().map(Student::getId).toList(),
                        g2.getEligibleStudents().stream().map(Student::getId).toList()
                    );
                    if (sameLecturer || sharedStudents) {
                        neighbors.computeIfAbsent(g1, k -> new HashSet<>()).add(g2);
                    }
                }
            }
        }
        // 3. Apply standard AC-3 loop with revise()
        Queue<Pair> queue = new LinkedList<>();
        for (var entry : neighbors.entrySet()) {
            for (SessionGroupMetaData neighbor : entry.getValue()) {
                queue.add(new Pair(entry.getKey(), neighbor));
            }
        }

        while (!queue.isEmpty()) {
            Pair arc = queue.poll();
            if (revise(domains, arc.xi, arc.xj)) {
                if (domains.get(arc.xi).isEmpty()) {
                    log.warn("Domain wiped out for: {}", arc.xi.getTypeGroup());
                }
                for (SessionGroupMetaData neighbor : neighbors.getOrDefault(arc.xi, Set.of())) {
                    if (!neighbor.equals(arc.xj)) {
                        queue.add(new Pair(neighbor, arc.xi));
                    }
                }
            }
        }

        return new AC3Result(domains, rejectionLogs);
    }

    private static boolean revise(Map<SessionGroupMetaData, List<AssignmentOption>> domains,
                                  SessionGroupMetaData xi, SessionGroupMetaData xj) {
        boolean revised = false;
        List<AssignmentOption> xiDomain = domains.get(xi);
        List<AssignmentOption> xjDomain = domains.get(xj);

        Iterator<AssignmentOption> it = xiDomain.iterator();

            while (it.hasNext()) {
                AssignmentOption a = it.next();
                boolean consistent = xjDomain.stream().anyMatch(b -> !conflict(a, b));
                if (!consistent) {
                    it.remove();
                    revised = true;
                }
            }

        return revised;
    }
        // 4. Return the pruned domain map
        private static boolean conflict(AssignmentOption a, AssignmentOption b) {
            int aStart = a.startSlot();
            int aEnd = a.startSlot() + 4;
            int bStart = b.startSlot();
            int bEnd = b.startSlot() + 4;

            return a.day() == b.day() && Math.max(aStart, bStart) < Math.min(aEnd, bEnd);

        }

    private record Pair(SessionGroupMetaData xi, SessionGroupMetaData xj) {}
    
}

