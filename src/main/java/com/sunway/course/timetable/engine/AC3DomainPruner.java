package com.sunway.course.timetable.engine;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

public class AC3DomainPruner {
    private static final Logger log = LoggerFactory.getLogger(AC3DomainPruner.class);

    public record AssignmentOption(int day, int startSlot, Venue venue) {}

    public static List<AssignmentOption> pruneDomain(
            LecturerAvailabilityMatrix lecturerMatrix,
            VenueAvailabilityMatrix venueMatrix,
            StudentAvailabilityMatrix studentMatrix,
            List<Venue> allVenues,
            SessionGroupMetaData meta,
            List<Student> eligibleStudents
    ) {
        List<AssignmentOption> domain = new ArrayList<>();

        int durationSlots = meta.getType().equalsIgnoreCase("Lecture") ? 4 : 4; // 2 hours (4 * 30-min)
        int maxDay = 5, maxSlot = 20;
        int requiredCapacity;

        for (int day = 0; day < maxDay; day++) {
            for (int start = 0; start <= maxSlot - durationSlots; start++) {
                int end = start + durationSlots;
                for (Venue venue : allVenues) {
                    if(meta.getType().equalsIgnoreCase("lecture")){
                        requiredCapacity = meta.getTotalStudents();
                    } else {
                        int groupsize = (int) Math.ceil((double) meta.getTotalStudents() / meta.getGroupCount());
                        requiredCapacity = groupsize;
                    }

                    if (venue.getCapacity() < requiredCapacity) continue;

                    boolean lecturerAvailable = lecturerMatrix.isAvailable(
                        meta.getLecturerName(), day, start, end);
                    boolean venueAvailable = venueMatrix.isAvailable(venue, start, end, day);

                    final int currentDay = day;
                    final int currentStart = start;
                    final int currentEnd = end;

                    List<Long> availableStudentIds = eligibleStudents.stream()
                        .map(Student::getId)
                        .filter(id -> studentMatrix.isAvailable(id, currentDay, currentStart, currentEnd))
                        .toList();

                    if (lecturerAvailable && venueAvailable && availableStudentIds.size() >= meta.getTotalStudents()) {
                        domain.add(new AssignmentOption(day, start, venue));
                    }
                }
            }
        }

        return domain;
    }
}

