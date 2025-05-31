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
            return schedule;
        });
    }

    // Check if lecturer is available at day, start..end slots
    public boolean isAvailable(String lecturerId, int day, int start, int end) {
        lock.readLock().lock();
        try {
            boolean[][] schedule = availability.get(lecturerId);
            if (schedule == null) return false;
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
            boolean[][] schedule = availability.get(lecturerId);
            if (schedule == null) {
                throw new IllegalStateException("Lecturer not registered: " + lecturerId);
            }
            for (int i = start; i < end; i++) {
                schedule[day][i] = true;
            }
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
}
