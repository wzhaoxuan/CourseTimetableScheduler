package com.sunway.course.timetable.singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

@Component
public class LecturerAvailabilityMatrix {
    private final int DAYS = 5;
    private final int TIME_SLOTS_PER_DAY = 20;

    private final Map<String, boolean[][]> availability = new HashMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Register a lecturer (create schedule matrix)
    public void registerLecturer(String lecturerId) {
        availability.computeIfAbsent(lecturerId, id -> {
            boolean[][] schedule = new boolean[DAYS][TIME_SLOTS_PER_DAY];
            for (boolean[] day : schedule) Arrays.fill(day, false);
            // System.out.printf("[LecturerMatrix] Registered lecturer %s%n", lecturerId);
            return schedule;
        });
    }

    // Check if lecturer is available at day, start..end slots
    public boolean isAvailable(String lecturerId, int day, int start, int end) {
        lock.readLock().lock();
        try {

            if (!isValidRange(day, start, end)) {
                System.err.printf("[LecturerMatrix] Invalid range: day=%d, start=%d, end=%d%n", day, start, end);
                return false;
            }
            boolean[][] schedule = availability.get(lecturerId);
            if (schedule == null) {
                // System.out.printf("[LecturerMatrix] Auto-registering unregistered lecturer: %s%n", lecturerId);
                lock.readLock().unlock(); // unlock read lock before write
                lock.writeLock().lock();
                try {
                    registerLecturer(lecturerId);
                } finally {
                    lock.readLock().lock(); // re-acquire read lock
                    lock.writeLock().unlock();
                }
                schedule = availability.get(lecturerId);
            }

            for (int i = start; i < end; i++) {
                if (schedule[day][i]) return false;
            }
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    // Assign lecturer at the slots (mark as busy)
    public void assign(String lecturerId, int day, int start, int end) {
        lock.writeLock().lock();
        try {
            if (!isValidRange(day, start, end)) {
                throw new IllegalArgumentException("Invalid slot range: day=" + day + ", start=" + start + ", end=" + end);
            }

            boolean[][] schedule = availability.computeIfAbsent(lecturerId, id -> {
                System.out.printf("[LecturerMatrix] Auto-registering during assign: %s%n", id);
                boolean[][] newSchedule = new boolean[DAYS][TIME_SLOTS_PER_DAY];
                for (boolean[] row : newSchedule) Arrays.fill(row, false);
                return newSchedule;
            });

            for (int i = start; i < end; i++) {
                schedule[day][i] = true;
            }
            // System.out.printf("[LecturerMatrix] Assigned %s on day=%d from %d to %d%n", lecturerId, day, start, end);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void printAvailability(String lecturerId) {
        lock.readLock().lock();
        try {
            boolean[][] schedule = availability.get(lecturerId);
            if (schedule == null) {
                System.out.println("Lecturer not found: " + lecturerId);
                return;
            }
            for (int day = 0; day < schedule.length; day++) {
                System.out.print("Day " + day + ": ");
                for (int slot = 0; slot < schedule[day].length; slot++) {
                    System.out.print(schedule[day][slot] ? "X" : "_");
                }
                System.out.println();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    // Range validation
    private boolean isValidRange(int day, int start, int end) {
        return day >= 0 && day < DAYS && start >= 0 && end <= TIME_SLOTS_PER_DAY && start < end;
    }
}
