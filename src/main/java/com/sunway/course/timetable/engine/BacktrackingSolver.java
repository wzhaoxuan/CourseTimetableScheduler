package com.sunway.course.timetable.engine;

import com.sunway.course.timetable.engine.constraint.interfaces.Constraint;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class BacktrackingSolver {
    public boolean solve(Map<Variable, TimeSlot> assignment, List<Variable> variables, List<Constraint> constraints) {
        if (assignment.size() == variables.size()) return true;

        Variable unassigned = variables.stream()
            .filter(v -> !assignment.containsKey(v))
            .min(Comparator.comparingInt(v -> v.getDomain().size())) // MRV
            .orElse(null);

        for (TimeSlot slot : unassigned.getDomain()) {
            boolean consistent = true;
            for (Map.Entry<Variable, TimeSlot> entry : assignment.entrySet()) {
                for (Constraint c : constraints) {
                    if (!c.isSatisfied(unassigned, slot, entry.getKey(), entry.getValue())) {
                        consistent = false;
                        break;
                    }
                }
                if (!consistent) break;
            }

            if (consistent) {
                assignment.put(unassigned, slot);
                if (solve(assignment, variables, constraints)) return true;
                assignment.remove(unassigned); // Backtrack
            }
        }

        return false;
    }
}
