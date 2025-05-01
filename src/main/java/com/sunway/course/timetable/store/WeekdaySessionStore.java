package com.sunway.course.timetable.store;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class WeekdaySessionStore extends SessionStore {
    private final Set<String> weekdays = new HashSet<>();

    @Override
    public boolean add(String weekdayName) {
        return weekdays.add(weekdayName); // Returns true if newly added
    }

    @Override
    public boolean contains(String weekdayName) {
        return weekdays.contains(weekdayName);
    }

    @Override
    public Set<String> get() {
        return new HashSet<>(weekdays);
    }

    @Override
    public void clear() {
        weekdays.clear();
    }
}
