package com.sunway.course.timetable.engine.constraint.hard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sunway.course.timetable.engine.TimeSlot;
import com.sunway.course.timetable.engine.Variable;
import com.sunway.course.timetable.engine.constraint.interfaces.Constraint;

public class ModuleConflictConstraint implements Constraint {

    private final List<Variable> involvedVariables;

    public ModuleConflictConstraint(List<Variable> allVariables) {
        this.involvedVariables = findInvolvedPairs(allVariables);
    }

    @Override
    public boolean isSatisfied(Variable v1, TimeSlot ts1, Variable v2, TimeSlot ts2) {
        if (!v1.equals(v2) && v1.getSession().getType().equals(v2.getSession().getType())) {
            return !ts1.overlapsWith(ts2);
        }
        return true;
    }

    @Override
    public List<Variable> getInvolvedVariables() {
        return involvedVariables != null ? involvedVariables : Collections.emptyList();
    }

    private List<Variable> findInvolvedPairs(List<Variable> vars) {
        Set<Variable> result = new HashSet<>();
        for (Variable v1 : vars) {
            for (Variable v2 : vars) {
                if (!v1.equals(v2) && v1.getSession().getType().equals(v2.getSession().getType())) {
                    result.add(v1);
                    result.add(v2);
                }
            }
        }
        return new ArrayList<>(result);
    }
}

