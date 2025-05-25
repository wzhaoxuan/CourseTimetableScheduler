package com.sunway.course.timetable.engine.constraint.interfaces;
import java.util.List;

import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.engine.Variable;

public interface BinaryConstraint {
    boolean isSatisfied(Variable v1, TimeSlot ts1, Variable v2, TimeSlot ts2);
    List<Variable> getInvolvedVariables();

    // Optional: Still useful if you need binary check
    default boolean involves(Variable a, Variable b) {
        List<Variable> vars = getInvolvedVariables();
        return vars.contains(a) && vars.contains(b);
    }
    
    default int getPenalty(Variable v1, TimeSlot ts1, Variable v2, TimeSlot ts2) {
        return isSatisfied(v1, ts1, v2, ts2) ? 0 : 1000; // 1 penalty if violated, 0 otherwise
    }

    default boolean isHard() {
        return true;
    }
}
