package com.sunway.course.timetable.engine.constraint.hard;

import java.util.Arrays;
import java.util.List;

import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.engine.Variable;
import com.sunway.course.timetable.engine.constraint.interfaces.Constraint;

public class StudentClashConstraint implements Constraint {

    private final Variable var1;
    private final Variable var2;

    public StudentClashConstraint(Variable var1, Variable var2) {
        this.var1 = var1;
        this.var2 = var2;
    }

    @Override
    public boolean isSatisfied(Variable v1, TimeSlot t1, Variable v2, TimeSlot t2) {
        if (!matches(v1, v2)) return true;

        if (v1.getSession().getStudent().equals(v2.getSession().getStudent())) {
            return !t1.overlapsWith(t2);
        }
        return true;
    }

    @Override
    public List<Variable> getInvolvedVariables() {
        return Arrays.asList(var1, var2);
    }

    private boolean matches(Variable a, Variable b) {
        return (a.equals(var1) && b.equals(var2)) || (a.equals(var2) && b.equals(var1));
    }

    @Override
    public String toString() {
        return "StudentClashConstraint{" +
                "var1=" + var1 +
                ", var2=" + var2 +
                '}';
    }
}
