package com.sunway.course.timetable.singleton;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Singleton class to manage venue assignments for sessions across different days and hours.
 * This class provides methods to check availability, assign sessions, and retrieve assignments.
 */
public class VenueAssignmentTable {
    private static final VenueAssignmentTable INSTANCE = new VenueAssignmentTable();

    // 3D structure: Day -> Hour -> Venue -> Session
    private final Map<String, Map<LocalTime, Map<Venue, Session>>> table = new HashMap<>();

    private VenueAssignmentTable() {}

    public static VenueAssignmentTable getInstance() {
        return INSTANCE;
    }

   public synchronized boolean isAvailable(String day, LocalTime startTime, LocalTime endTime, Venue venue) {
        for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusHours(1)) {
            if (!table.getOrDefault(day, new HashMap<>()).getOrDefault(time, new HashMap<>()).containsKey(venue)) continue;
            return false;
        }
        return true;
    }

    public synchronized void assign(String day, LocalTime startTime, LocalTime endTime, Venue venue, Session session) {
        for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusHours(1)) {
            table.computeIfAbsent(day, d -> new HashMap<>())
                 .computeIfAbsent(time, t -> new HashMap<>())
                 .put(venue, session);
        }
    }

    public synchronized Map<Venue, Session> getHourAssignments(String day, LocalTime hour) {
        return table.getOrDefault(day, new HashMap<>()).getOrDefault(hour, new HashMap<>());
    }

    public synchronized Optional<String> findNextAvailableDay(LocalTime hour, Venue venue) {
        for (String day : table.keySet()) {
            if (isAvailable(day, hour, hour.plusHours(1), venue)) {
                return Optional.of(day);
            }
        }
        return Optional.empty();
    }
}

