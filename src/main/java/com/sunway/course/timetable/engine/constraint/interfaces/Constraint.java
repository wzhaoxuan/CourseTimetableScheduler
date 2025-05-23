package com.sunway.course.timetable.engine.constraint.interfaces;
import java.util.List;

import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.engine.Variable;

public interface Constraint {
    boolean isSatisfied(Variable v1, TimeSlot ts1, Variable v2, TimeSlot ts2);

    // Optional: Still useful if you need binary check
    default boolean involves(Variable a, Variable b) {
        List<Variable> vars = getInvolvedVariables();
        return vars.contains(a) && vars.contains(b);
    }
    
    List<Variable> getInvolvedVariables();
}
