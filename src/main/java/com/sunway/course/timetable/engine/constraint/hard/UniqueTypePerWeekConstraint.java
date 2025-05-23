package com.sunway.course.timetable.engine.constraint.hard;

import java.util.Arrays;
import java.util.List;

import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.engine.Variable;
import com.sunway.course.timetable.engine.constraint.interfaces.Constraint;

public class UniqueTypePerWeekConstraint implements Constraint {

    private final Variable v1;
    private final Variable v2;

    public UniqueTypePerWeekConstraint(Variable v1, Variable v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public boolean isSatisfied(Variable a, TimeSlot t1, Variable b, TimeSlot t2) {
        if ((a.equals(v1) && b.equals(v2)) || (a.equals(v2) && b.equals(v1))) {
            if (v1.getSession().getType().equals(v2.getSession().getType())
                    && v1.getSession().gettype_group().equals(v2.getSession().gettype_group())) {
                return !t1.getDay().equals(t2.getDay());
            }
        }
        return true;
    }

    @Override
    public List<Variable> getInvolvedVariables() {
        return Arrays.asList(v1, v2);
    }
}
