package com.sunway.course.timetable.service.calculator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.singleton.LecturerAvailabilityMatrix;
import com.sunway.course.timetable.singleton.StudentAvailabilityMatrix;
import com.sunway.course.timetable.singleton.VenueAvailabilityMatrix;

/**
 * FitnessCalculator is responsible for calculating the fitness score of a session candidate
 * based on hard and soft constraints.
 */
@Service
public class FitnessCalculator {

    // Define your constraint weights
    private static final double ALPHA_LECTURER_UNAVAILABLE = 1000.0;
    private static final double ALPHA_VENUE_UNAVAILABLE = 1000.0;
    private static final double ALPHA_STUDENT_CONFLICT = 1000.0;

    private static final double BETA_EARLY_SLOT = 5.0;
    private static final double BETA_COMPACT_DAY = 3.0;
    private static final double BETA_SAME_DAY_GROUP = 2.0;

    public double calculateFitness(SessionCandidate candidate,
                                   LecturerAvailabilityMatrix lecturerMatrix,
                                   VenueAvailabilityMatrix venueMatrix,
                                   StudentAvailabilityMatrix studentMatrix,
                                   List<String> studentPreferredVenues,
                                   Map<Long, String> studentProgrammeMap) {

        double score = 0.0;

        // === Hard Constraints ===

        boolean lecturerAvailable = lecturerMatrix.isAvailable(
                candidate.lecturer, candidate.day, candidate.startSlot, candidate.endSlot);

        boolean venueAvailable = venueMatrix.isAvailable(
                candidate.venue, candidate.startSlot, candidate.endSlot, candidate.day);

        boolean allStudentsAvailable = candidate.students.stream()
                .allMatch(s -> studentMatrix.isAvailable(s.getId(), candidate.day, candidate.startSlot, candidate.endSlot - candidate.startSlot));

        score += ALPHA_LECTURER_UNAVAILABLE * (lecturerAvailable ? 0 : 1);
        score += ALPHA_VENUE_UNAVAILABLE * (venueAvailable ? 0 : 1);
        score += ALPHA_STUDENT_CONFLICT * (allStudentsAvailable ? 0 : 1);

        // === Soft Constraints ===

        boolean earlySlot = candidate.startSlot < 4; // before 10AM if slot=0 starts at 8AM
        score += BETA_EARLY_SLOT * (earlySlot ? 0 : 1);

        boolean allSameDay = candidate.students.stream()
            .map(s -> studentProgrammeMap.get(s.getId()))
            .distinct()
            .count() == 1;

        score += BETA_SAME_DAY_GROUP * (allSameDay ? 0 : 1);

        boolean venuePreferred = studentPreferredVenues.contains(String.valueOf(candidate.venue.getId()));
        score += BETA_COMPACT_DAY * (venuePreferred ? 0 : 1);

        return score;
    }

    public static class SessionCandidate {
        public int day;
        public int startSlot;
        public int endSlot;
        public Venue venue;
        public String lecturer;
        public List<Student> students;
        public String sessionType;

        public SessionCandidate(int day, int startSlot, int endSlot, Venue venue, String lecturer,
                                 List<Student> students, String sessionType) {
            this.day = day;
            this.startSlot = startSlot;
            this.endSlot = endSlot;
            this.venue = venue;
            this.lecturer = lecturer;
            this.students = students;
            this.sessionType = sessionType;
        }
    }
}

