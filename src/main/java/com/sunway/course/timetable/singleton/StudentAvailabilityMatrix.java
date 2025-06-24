package com.sunway.course.timetable.singleton;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Student;

/**
 * Track each student's availability across multiple days and time slots.
 */
@Component
public class StudentAvailabilityMatrix {

    private static final int DAYS = 5; // Monday to Friday
    private static final int TIME_SLOTS_PER_DAY = 20; // e.g. 8am–8pm, 1 hour each

    private final Map<Long, boolean[][]> availabilityMap = new ConcurrentHashMap<>();

    /**
     * Initialize all students as fully available.
     */
    public void initializeStudents(List<Student> students) {
        for (Student student : students) {
            boolean[][] matrix = new boolean[DAYS][TIME_SLOTS_PER_DAY];
            for (int day = 0; day < DAYS; day++) {
                for (int hour = 0; hour < TIME_SLOTS_PER_DAY; hour++) {
                    matrix[day][hour] = true; // available by default
                }
            }
            availabilityMap.put(student.getId(), matrix);
        }
    }

    /**
     * Check if a student is available at a specific time slot.
     */
    public boolean isAvailable(Long studentId, int day, int startHour, int duration) {
        boolean[][] matrix = availabilityMap.get(studentId);
        if (matrix == null) return false;

        for (int h = startHour; h < startHour + duration && h < TIME_SLOTS_PER_DAY; h++) {
            if (!matrix[day][h]) return false;
        }
        return true;
    }

    /**
     * Mark the given time range as unavailable for a student.
     */
    public synchronized void markUnavailable(Long studentId, int day, int startHour, int duration) {
        boolean[][] matrix = availabilityMap.get(studentId);
        if (matrix != null) {
            for (int h = startHour; h < startHour + duration && h < TIME_SLOTS_PER_DAY; h++) {
                matrix[day][h] = false;
            }
        }
    }

    /**
     * Mark the given time range as available for a student.
     */
    public synchronized void markAvailable(Long studentId, int day, int startHour, int duration) {
        boolean[][] matrix = availabilityMap.get(studentId);
        if (matrix != null) {
            for (int h = startHour; h < startHour + duration && h < TIME_SLOTS_PER_DAY; h++) {
                matrix[day][h] = true;
            }
        }
    }

    public List<Long> findAvailableStudents(Set<Long> candidates, int day, int startSlot, int durationSlots, int max) {
        List<Long> available = new ArrayList<>();
        for (Long id : candidates) {
            if (isAvailable(id, day, startSlot, durationSlots)) {
                available.add(id);
                if (available.size() >= max) break;
            }
        }
        return available;
    }

    public synchronized void reset() {
        for (boolean[][] matrix : availabilityMap.values()) {
            for (int day = 0; day < DAYS; day++) {
                for (int hour = 0; hour < TIME_SLOTS_PER_DAY; hour++) {
                    matrix[day][hour] = true;
                }
            }
        }
    }

    /**
     * Get list of assigned session start times (in 30-min slots) for the given student on a given day.
     * @return List of LocalTime representing occupied time blocks.
     */
    public List<LocalTime> getAssignedTimes(long studentId, int day) {
        List<LocalTime> times = new ArrayList<>();
        boolean[][] matrix = availabilityMap.get(studentId);
        if (matrix == null) return times;

        for (int slot = 0; slot < TIME_SLOTS_PER_DAY; slot++) {
            if (!matrix[day][slot]) {
                // Convert slot index to time: 8:00 AM + slot * 30 minutes
                LocalTime time = LocalTime.of(8, 0).plusMinutes(slot * 30L);
                times.add(time);
            }
        }

        return times;
    }

    /**
     * Check if assigning a session would result in the student having only one session on that day.
     * This is used to avoid scheduling a session that would leave the student with no other commitments.
     */
    public boolean wouldBeOnlySession(long studentId, int day, int candidateStart, int duration) {
        boolean[][] matrix = availabilityMap.get(studentId);
        if (matrix == null) return false;

        for (int slot = 0; slot < TIME_SLOTS_PER_DAY; slot++) {
            // If student is busy in some slot
            if (!matrix[day][slot]) {
                boolean isInsideCandidate = slot >= candidateStart && slot < candidateStart + duration;
                if (!isInsideCandidate) {
                    // Found another session outside the one being checked
                    return false;
                }
            }
        }

        // Either no sessions at all, or only sessions match the candidate block
        return true;
    }
}
