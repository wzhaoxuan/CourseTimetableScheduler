package com.sunway.course.timetable.store;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Collections;


import org.springframework.stereotype.Component;

@Component
public class WeekdaySessionStore {
    private final Map<Long, Set<String>> lecturerAvailability = new HashMap<>();

    public boolean add(Long lecturerId, List<String> availableDays) {
        return lecturerAvailability
            .computeIfAbsent(lecturerId, id -> new HashSet<>())
            .addAll(availableDays);
    }

    public Set<String> getAvailableDays(Long lecturerId) {
        return lecturerAvailability.getOrDefault(lecturerId, Collections.emptySet());
    }

    public boolean isAvailable(Long lecturerId, DayOfWeek day) {
        return lecturerAvailability.getOrDefault(lecturerId, Collections.emptySet()).contains(day);
    }

    public void clear() {
        lecturerAvailability.clear();
    }

    public Map<Long, Set<String>> getAllAvailability() {
        return new HashMap<>(lecturerAvailability);
    }
}
