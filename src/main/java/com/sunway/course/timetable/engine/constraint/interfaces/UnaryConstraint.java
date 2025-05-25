package com.sunway.course.timetable.engine.constraint.interfaces;
import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.engine.Variable;

public interface UnaryConstraint {
    boolean isSatisfied(Variable v, TimeSlot ts);
    
    default int getPenalty(Variable v, TimeSlot ts) {
        return isSatisfied(v, ts) ? 0 : 1;
    }

    default boolean isHard() {
        return false;
    }

}

