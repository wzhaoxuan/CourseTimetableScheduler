package com.sunway.course.timetable.singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

/**
 *  Singleton class to manage lecturer availability matrix.
 *  This class maintains a schedule matrix for each lecturer,
 *   where each matrix is a 2D array representing days and time slots.
 *  *  Each cell in the matrix indicates whether the lecturer is available (false) or busy (true)
 *  *  The matrix has 5 days (0-4) and 20 time slots per day (0-19).
 */
@Component
public class LecturerAvailabilityMatrix {

    private final int DAYS = 5;
    private final int TIME_SLOTS_PER_DAY = 20;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, boolean[][]> availability = new HashMap<>();

    /**
     * Registers a new lecturer with an empty availability matrix.
     * 
     * @param lecturerId
     */
    public void registerLecturer(String lecturerId) {
        availability.computeIfAbsent(lecturerId, id -> {
            boolean[][] schedule = new boolean[DAYS][TIME_SLOTS_PER_DAY];
            for (boolean[] day : schedule) Arrays.fill(day, false);
            return schedule;
        });
    }

    /**
     * Checks if a lecturer is available for a given time slot.
     * This method checks if the lecturer is registered,
     * and if not, it automatically registers them.
     * 
     * @param lecturerId
     * @param day
     * @param start
     * @param end
     * @return
     */
    public boolean isAvailable(String lecturerId, int day, int start, int end) {
        lock.readLock().lock();
        try {
            if (!isValidRange(day, start, end)) {
                return false;
            }
            boolean[][] schedule = availability.get(lecturerId);
            if (schedule == null) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    registerLecturer(lecturerId);
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
                schedule = availability.get(lecturerId);
            }

            for (int i = start; i < end; i++) {
                if (schedule[day][i]){
                    return false;
                }
            }
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Assigns a time slot to a lecturer, marking them as busy.
     * This method will throw an exception if the time slot is invalid
     * or if the lecturer is not registered.
     * 
     * @param lecturerId
     * @param day
     * @param start
     * @param end
     */
    public void assign(String lecturerId, int day, int start, int end) {
        lock.writeLock().lock();
        try {
            if (!isValidRange(day, start, end)) {
                throw new IllegalArgumentException("Invalid slot range: day=" + day + 
                ", start=" + start + ", end=" + end);
            }

            boolean[][] schedule = availability.computeIfAbsent(lecturerId, id -> {
                boolean[][] newSchedule = new boolean[DAYS][TIME_SLOTS_PER_DAY];
                for (boolean[] row : newSchedule) Arrays.fill(row, false);
                return newSchedule;
            });

            for (int i = start; i < end; i++) {
                schedule[day][i] = true; // mark as busy
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public synchronized void reset() {
        for (boolean[][] matrix : availability.values()) {
            for (int day = 0; day < DAYS; day++) {
                for (int hour = 0; hour < TIME_SLOTS_PER_DAY; hour++) {
                    matrix[day][hour] = false; // false = available
                }
            }
        }
    }

    /**
     * Validates the range of day and time slots.
     * This method checks if the day is within the range of 0 to 4,
     * and if the start and end time slots are within the range of 0 to 19.
     * 
     * @param day
     * @param start
     * @param end
     * @return
     */
    private boolean isValidRange(int day, int start, int end) {
        return day >= 0 && day < DAYS && start >= 0 && 
                end <= TIME_SLOTS_PER_DAY && start < end;
    }

    /**
     * Retrieves the set of days on which a lecturer is assigned.
     * This method returns a set of integers representing the days
     * (0-4) on which the lecturer has at least one time slot assigned.
     * 
     * @param lecturerId
     * @return Set of days (0-4) on which the lecturer is assigned
     */
    public Set<Integer> getAssignedDays(String lecturerId) {
        lock.readLock().lock();
        try {
            boolean[][] schedule = availability.get(lecturerId);
            if (schedule == null) return Collections.emptySet();

            Set<Integer> assignedDays = new HashSet<>();
            for (int day = 0; day < DAYS; day++) {
                for (int slot = 0; slot < TIME_SLOTS_PER_DAY; slot++) {
                    if (schedule[day][slot]) {
                        assignedDays.add(day);
                        break; 
                    }
                }
            }
            return assignedDays;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves the daily availability array for a lecturer.
     * This method returns a boolean array representing the availability
     * of the lecturer for a specific day, where false indicates available
     * and true indicates busy.
     * 
     * @param lecturerId
     * @param day
     * @return boolean array of availability for the specified day
     */
    public boolean[] getDailyAvailabilityArray(String lecturerId, int day) {
        lock.readLock().lock();
        try {
            boolean[][] schedule = availability.get(lecturerId);
            if (schedule == null || day < 0 || day >= DAYS) {
                return new boolean[TIME_SLOTS_PER_DAY]; 
            }
            return Arrays.copyOf(schedule[day], TIME_SLOTS_PER_DAY); 
        } finally {

            lock.readLock().unlock();
        }
    }
}
