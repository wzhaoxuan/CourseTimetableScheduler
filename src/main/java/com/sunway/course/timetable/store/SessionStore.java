package com.sunway.course.timetable.store;
import java.util.Set;

public abstract class SessionStore {
    public abstract boolean add(String name);

    public abstract boolean contains(String name);

    public abstract Set<String> get();

    public abstract void clear();

}
