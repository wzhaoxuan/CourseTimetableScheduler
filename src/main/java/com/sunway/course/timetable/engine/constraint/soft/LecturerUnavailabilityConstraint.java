package com.sunway.course.timetable.engine.constraint.soft;

import java.util.Map;
import java.util.Set;

import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.engine.Variable;
import com.sunway.course.timetable.engine.constraint.interfaces.UnaryConstraint;

public class LecturerUnavailabilityConstraint implements UnaryConstraint {

    private final Map<Long, Set<String>> unavailableDaysByLecturerId;

    public LecturerUnavailabilityConstraint(Map<Long, Set<String>> unavailableDaysByLecturerId) {
        this.unavailableDaysByLecturerId = unavailableDaysByLecturerId;
    }

    @Override
    public boolean isSatisfied(Variable v, TimeSlot ts) {
        Long lecturerId = v.getLecturerId(); // Make sure Variable has this method
        if (lecturerId == null || ts == null) return true;

        Set<String> unavailableDays = unavailableDaysByLecturerId.get(lecturerId);
        if (unavailableDays == null) return true;

        String assignedDay = ts.getDay().name(); // Ensure TimeSlot has getDay(): DayOfWeek
        return !unavailableDays.contains(assignedDay);
    }

    @Override
    public int getPenalty(Variable v, TimeSlot ts) {
        return isSatisfied(v, ts) ? 0 : 10; // Simple penalty of 1 if violated
    }

    @Override
    public boolean isHard() {
        return false; // Marked as soft constraint
    }
}
