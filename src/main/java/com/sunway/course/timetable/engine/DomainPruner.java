package com.sunway.course.timetable.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.model.Lecturer;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.service.LecturerServiceImpl;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;
import com.sunway.course.timetable.util.LecturerDayAvailabilityUtil;

public class DomainPruner {
    private static final Logger log = LoggerFactory.getLogger(DomainPruner.class);

    public record AssignmentOption(int day, int startSlot, Venue venue) {}

    public static List<AssignmentOption> pruneDomain(
            LecturerAvailabilityMatrix lecturerMatrix,
            VenueAvailabilityMatrix venueMatrix,
            StudentAvailabilityMatrix studentMatrix,
            List<Venue> allVenues,
            SessionGroupMetaData meta,
            List<Student> eligibleStudents,
            LecturerServiceImpl lecturerService,
            LecturerDayAvailabilityUtil lecturerDayAvailabilityUtil,
            List<DomainRejectionReason> rejectionLogs
    ) {
        List<AssignmentOption> domain = new ArrayList<>();

        int durationSlots = meta.getType().equalsIgnoreCase("Lecture") ? 4 : 4; // 2 hours (4 * 30-min)
        int maxDay = 5, maxSlot = 20;
        int requiredCapacity;
        
        Optional<Lecturer> lecturerOpt = lecturerService.getLecturerByName(meta.getLecturerName());
        log.info("Pruning domain for {} with lecturer {}", meta.getTypeGroup(), meta.getLecturerName());
        if (lecturerOpt.isEmpty()) {
            log.warn("Lecturer {} not found, skipping pruning.", meta.getLecturerName());
            return domain;
        }

        Long lecturerId = lecturerOpt.get().getId();

        for (int day = 0; day < maxDay; day++) {

            // Skip days where lecturer is marked unavailable (e.g., from weekday constraint)
            String dayName = switch (day) {
                        case 0 -> "Monday";
                        case 1 -> "Tuesday";
                        case 2 -> "Wednesday";
                        case 3 -> "Thursday";
                        case 4 -> "Friday";
                        default -> "Unknown";
            };

            if (lecturerDayAvailabilityUtil.isUnavailable(lecturerId, dayName)) {
                continue;
            }

            for (int start = 0; start <= maxSlot - durationSlots; start++) {
                int end = start + durationSlots;
                for (Venue venue : allVenues) {
                    if(meta.getType().equalsIgnoreCase("lecture")){
                        requiredCapacity = meta.getTotalStudents();
                    } else {
                        int groupsize = (int) Math.ceil((double) meta.getTotalStudents() / meta.getGroupCount());
                        requiredCapacity = groupsize;
                    }

                    // capacity check
                    if (venue.getCapacity() < requiredCapacity) {
                        rejectionLogs.add(new DomainRejectionReason(meta, day, start, venue.getName(), "Venue too small"));
                        continue;
                    }

                    // weekday constraint check
                    if (lecturerDayAvailabilityUtil.isUnavailable(lecturerId, dayName)) {
                        rejectionLogs.add(new DomainRejectionReason(meta, day, start, venue.getName(), "Lecturer unavailable (weekday constraint)"));
                        continue;
                    }

                    // lecturer schedule conflict
                    boolean lecturerAvailable = lecturerMatrix.isAvailable(meta.getLecturerName(), day, start, end);
                    if (!lecturerAvailable) {
                        rejectionLogs.add(new DomainRejectionReason(meta, day, start, venue.getName(), "Lecturer busy"));
                        continue;
                    }

                    // venue conflict
                    boolean venueAvailable = venueMatrix.isAvailable(venue, start, end, day);
                    if (!venueAvailable) {
                        rejectionLogs.add(new DomainRejectionReason(meta, day, start, venue.getName(), "Venue occupied"));
                        continue;
                    }

                    final int currentDay = day;
                    final int currentStart = start;
                    final int currentEnd = end;

                    List<Long> availableStudentIds = eligibleStudents.stream()
                        .map(Student::getId)
                        .filter(id -> studentMatrix.isAvailable(id, currentDay, currentStart, currentEnd))
                        .toList();

                    // if (lecturerAvailable && venueAvailable && availableStudentIds.size() >= meta.getTotalStudents()) {
                    //     domain.add(new AssignmentOption(day, start, venue));
                    // }

                    if (availableStudentIds.size() < meta.getTotalStudents()) {
                        rejectionLogs.add(new DomainRejectionReason(meta, day, start, venue.getName(), 
                            String.format("Student clash (only %d available, need %d)", 
                            availableStudentIds.size(), meta.getTotalStudents())));
                        continue;
                    }
                    
                    domain.add(new AssignmentOption(day, start, venue));
                }
            }
        }

        return domain;
    }
}

