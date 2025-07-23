package com.sunway.course.timetable.singleton;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Student;

/**
 * Track each student's availability across multiple days and time slots.
 */
@Component
public class StudentAvailabilityMatrix {

    private static final int DAYS = 5; // Monday to Friday
    private static final int TIME_SLOTS_PER_DAY = 20; // e.g. 8am–6pm
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<Long, boolean[][]> availabilityMap = new HashMap<>();
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
        lock.readLock().lock();
        try {
            boolean[][] matrix = availabilityMap.get(studentId);
            if (matrix == null) return false;

            for (int h = startHour; h < startHour + duration && h < TIME_SLOTS_PER_DAY; h++) {
                if (!matrix[day][h]) return false;
            }
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Mark the given time range as unavailable for a student.
     */
    public synchronized void assign(Long studentId, int day, int startHour, int duration) {
        lock.writeLock().lock();
        try{
            boolean[][] matrix = availabilityMap.get(studentId);
            if (matrix != null) {
                for (int h = startHour; h < startHour + duration && h < TIME_SLOTS_PER_DAY; h++) {
                    matrix[day][h] = false;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
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
    public List<LocalTime> getAssignedDays(long studentId, int day) {
        lock.readLock().lock();
        try {
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
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Check if assigning a session would result in the student having only one session on that day.
     * This is used to avoid scheduling a session that would leave the student with no other commitments.
     */
    public boolean wouldBeOnlySession(long studentId, int day, int candidateStart, int duration) {
        lock.readLock().lock();
        try{
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
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean[] getDailyAvailabilityArray(long studentId, int day) {
        lock.readLock().lock();
        try {
            boolean[][] schedule = availabilityMap.get(studentId);
            if (schedule == null || day < 0 || day >= DAYS) {
                return new boolean[TIME_SLOTS_PER_DAY]; 
            }
            return Arrays.copyOf(schedule[day], TIME_SLOTS_PER_DAY); 
        } finally {

            lock.readLock().unlock();
        }
    }
}
