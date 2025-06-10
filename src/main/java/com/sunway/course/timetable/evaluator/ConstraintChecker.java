package com.sunway.course.timetable.evaluator;

import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;

public interface ConstraintChecker {
    String getName();
    double getWeight();
    ConstraintType getType();
    double getPenalty(List<Session> sessions, Map<Session, Venue> sessionVenueMap);
}
