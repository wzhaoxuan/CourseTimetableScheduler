package com.sunway.course.timetable.singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private boolean[][][] availability; // [venueIndex][timeSlot][dayIndex]
    private List<Venue> venues;
    private Map<Long, Integer> venueIndexMap; // venueId -> index

    public VenueAvailabilityMatrix(List<Venue> venues) {
        this.venues = venues;
    }

    /**
     * Must be called after application context startup to initialize matrix.
     */
    @PostConstruct
    public void initialize() {
        int venueCount = venues.size();
        this.availability = new boolean[venueCount][TIME_SLOTS_PER_DAY][DAYS];
        this.venueIndexMap = new HashMap<>();

        for (int i = 0; i < venueCount; i++) {
            venueIndexMap.put(venues.get(i).getId(), i);
        }
    }

    /**
     * Checks if the venue is available for the specified time range on a given day.
     * 
     * @param venue The venue to check
     * @param startIndex The start time index (inclusive)
     * @param endIndex The end time index (exclusive)
     * @param dayIndex The day index (0-4 for Monday to Friday)
     * @return true if the venue is available for the specified time range, false if it is occupied
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
     * 
     * @param venue The venue to assign
     * @param startIndex The start time index (inclusive)
     * @param endIndex The end time index (exclusive)
     * @param dayIndex The day index (0-4 for Monday to Friday)
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

    public void reset() {
        for (int v = 0; v < availability.length; v++) {
            for (int t = 0; t < TIME_SLOTS_PER_DAY; t++) {
                for (int d = 0; d < DAYS; d++) {
                    availability[v][t][d] = false;
                }
            }
        }
    }


    /**
     * Returns an unmodifiable list of all venues.
     * 
     * @return List of Venue objects
     */
    public List<Venue> getVenues() {
        return Collections.unmodifiableList(venues);
    }

    /**
     * Returns the index of the venue in the matrix.
     * 
     * @param venue The venue to find
     * @return The index of the venue, or null if not found
     */
    public Integer getIndexForVenue(Venue venue) {
        return venueIndexMap.get(venue.getId());
    }

    /**
     * Returns the availability matrix.
     * 
     * @return 3D boolean array representing availability
     */
    public boolean[][][] getAvailability() {
        return availability;
    }
}
