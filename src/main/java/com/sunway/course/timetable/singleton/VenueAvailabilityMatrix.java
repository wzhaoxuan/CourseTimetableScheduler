package com.sunway.course.timetable.singleton;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Venue;

import jakarta.annotation.PostConstruct;


/**
 * Singleton class to manage venue assignments for sessions across different days and time slots.
 * The availability boolean array is indexed as [venue][timeSlot][day].
 *  This class provides methods to check availability, assign venues,
 * 
 */
@Component
public class VenueAvailabilityMatrix {

    private static final int DAYS = 5; // Monday to Friday
    private static final int TIME_SLOTS_PER_DAY = 20; // 8:00 to 18:00 in 30-minute increments
    private static final int START_HOUR = 8;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private boolean[][][] availability; // [venueIndex][timeSlot][dayIndex]
    private List<Venue> sortedVenues;
    private Map<Long, Integer> venueIndexMap; // venueId -> index

    public VenueAvailabilityMatrix(List<Venue> sortedVenues) {
        this.sortedVenues = sortedVenues;
    }

    /**
     * Must be called after application context startup to initialize matrix.
     */
    @PostConstruct
    public void initialize() {
        int venueCount = sortedVenues.size();
        this.availability = new boolean[venueCount][TIME_SLOTS_PER_DAY][DAYS];
        this.venueIndexMap = new HashMap<>();

        for (int i = 0; i < venueCount; i++) {
            venueIndexMap.put(sortedVenues.get(i).getId(), i);
        }

        // for (Venue v : sortedVenues) {
        //     System.out.println("[INIT] Venue: " + v.getName() + " cap=" + v.getCapacity());
        // }
    }

    /**
     * Checks if the venue is available on given dayIndex between startIndex (inclusive) and endIndex (exclusive).
     */
    public boolean isAvailable(Venue venue, int startIndex, int endIndex, int dayIndex) {
        lock.readLock().lock();

        try {
            Integer venueIndex = venueIndexMap.get(venue.getId());
            if (venueIndex == null) return true;

            for (int i = startIndex; i < endIndex; i++) {
                if (!availability[venueIndex][i][dayIndex]) {
                    return true; 
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Assigns the venue to occupied between startIndex (inclusive) and endIndex (exclusive) on dayIndex.
     */
    public void assign(Venue venue, int startIndex, int endIndex, int dayIndex) {
        lock.writeLock().lock();
        try {
            Integer venueIndex = venueIndexMap.get(venue.getId());
            if (venueIndex == null) return;

            for (int i = startIndex; i < endIndex; i++) {
                availability[venueIndex][i][dayIndex] = true; // mark as occupied
            }
        } finally {
            lock.writeLock().unlock();
        }
        
    }

    /**
     * Converts DayOfWeek (MON-FRI) to day index 0-4.
     */
    public int dayToIndex(DayOfWeek day) {
        return day.getValue() - 1; // MONDAY=1 → 0 index
    }

    /**
     * Converts time to index (0-based) for 30-minute slots from 8:00am.
     * Example: 8:00 = 0, 8:30 = 1, 9:00 = 2, ..., 18:00 = 20 (exclusive upper bound)
     */
    public int timeToIndex(int hour, int minute) {
        return ((hour - START_HOUR) * 60 + minute) / 30;
    }

    /**
     * Converts index back to time in minutes from midnight.
     */
    public int indexToMinutes(int index) {
        return START_HOUR * 60 + index * 30;
    }

    public String indexToTimeString(int index) {
        int totalMinutes = indexToMinutes(index);
        int hour = totalMinutes / 60;
        int minute = totalMinutes % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Result object for assigned venue and time slot.
     */
    public record VenueAssignmentResult(Venue venue, int startIndex, int durationSlots) {}

    /**
     * Tries to find the first available venue and time slot for a given session duration and minimum capacity,
     * scanning day by day, and for each day, iterating through sorted venues.
     * 
     * @param durationHours how long the session should last (in hours)
     * @param minCapacity minimum number of seats required
     * @return Optional of VenueAssignmentResult if successful
     */
    public Optional<VenueAssignmentResult> findAndAssignByVenueThenDay(int durationHours, int minCapacity) {
        int durationSlots = durationHours * 2;

        lock.writeLock().lock(); // important: must lock the whole check+assign together
        try {
            for (int dayIndex = 0; dayIndex < DAYS; dayIndex++) {
                for (Venue venue : sortedVenues) {
                    if (venue.getCapacity() < minCapacity) continue;

                    for (int startIndex = 0; startIndex <= TIME_SLOTS_PER_DAY - durationSlots; startIndex++) {
                        int endIndex = startIndex + durationSlots;

                        if (isAvailable(venue, startIndex, endIndex, dayIndex)) {
                            assign(venue, startIndex, endIndex, dayIndex);
                            return Optional.of(new VenueAssignmentResult(venue, startIndex, durationSlots));
                        }
                    }
                }
            }

            return Optional.empty();
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * Returns the sorted list of venues used in the matrix.
     */
    public List<Venue> getSortedVenues() {
        return Collections.unmodifiableList(sortedVenues);
    }

    /**
     * For testing or debugging: prints current availability matrix.
     */
    public void printAvailability() {
        lock.readLock().lock();
        try{
            for (int v = 0; v < sortedVenues.size(); v++) {
                System.out.println("Venue: " + sortedVenues.get(v).getName() + " (Capacity: " + sortedVenues.get(v).getCapacity() + ")");
                for (int d = 0; d < DAYS; d++) {
                    System.out.print("Day " + (d+1) + ": ");
                    for (int t = 0; t < TIME_SLOTS_PER_DAY; t++) {
                        System.out.print(availability[v][t][d] ? "X" : "_");
                    }
                    System.out.println();
                }
                System.out.println();
            }
        } finally {
            lock.readLock().unlock();
        }
        
    }

    public Integer getIndexForVenue(Venue venue) {
        return venueIndexMap.get(venue.getId());
    }


    public boolean[][][] getAvailability() {
        return availability;
    }
}
