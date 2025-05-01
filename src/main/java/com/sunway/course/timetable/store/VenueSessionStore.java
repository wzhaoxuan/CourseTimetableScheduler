package com.sunway.course.timetable.store;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class VenueSessionStore extends SessionStore {
    private final Set<String> venues = new HashSet<>();

    @Override
    public boolean add(String venueName) {
        return venues.add(venueName); // Returns true if newly added
    }

    @Override
    public boolean contains(String venueName) {
        return venues.contains(venueName);
    }

    @Override
    public Set<String> get() {
        return new HashSet<>(venues);
    }

    @Override
    public void clear() {
        venues.clear();
    }

}
