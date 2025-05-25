package com.sunway.course.timetable.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sunway.course.timetable.engine.constraint.interfaces.BinaryConstraint;

import javafx.util.Pair;

public class AC3 {

    public boolean runAC3(List<Variable> variables, List<BinaryConstraint> constraints) {
        Queue<Pair<Variable, Variable>> queue = new LinkedList<>();
        Set<Pair<Variable, Variable>> seen = new HashSet<>();
        Map<Variable, List<BinaryConstraint>> constraintMap = buildConstraintMap(variables, constraints);

        // Initialize queue with arcs that are involved in at least one constraint
        for (BinaryConstraint constraint : constraints) {
            List<Variable> involved = constraint.getInvolvedVariables();

            if (involved.size() == 2) {
                Variable vi = involved.get(0);
                Variable vj = involved.get(1);

                // Add arc (vi -> vj) if not seen before
                Pair<Variable, Variable> arc = new Pair<>(vi, vj);
                if (seen.add(arc)) queue.add(arc);

                // Add arc (vj -> vi) if not seen before
                Pair<Variable, Variable> reverseArc = new Pair<>(vj, vi);
                if (seen.add(reverseArc)) queue.add(reverseArc);
            }

            // Iterate over pairs of involved variables without repetition
            // for (int i = 0; i < involved.size(); i++) {
            //     for (int j = i + 1; j < involved.size(); j++) {
            //         Variable vi = involved.get(i);
            //         Variable vj = involved.get(j);

            //         // Add arc (vi -> vj) if not seen before
            //         Pair<Variable, Variable> arc = new Pair<>(vi, vj);
            //         if (seen.add(arc)) queue.add(arc);

            //         // Add arc (vj -> vi) if not seen before
            //         Pair<Variable, Variable> reverseArc = new Pair<>(vj, vi);
            //         if (seen.add(reverseArc)) queue.add(reverseArc);
            //     }
            // }
        }

        int iterations = 0;

        while (!queue.isEmpty()) {
            iterations++;
            if (iterations % 100 == 0) {
                // System.out.println("Iteration: " + iterations + ", Queue size: " + queue.size() + ", Seen size: " + seen.size());
            }
            Pair<Variable, Variable> arc = queue.poll();
            Variable xi = arc.getKey();
            Variable xj = arc.getValue();

            // If revising xi's domain with respect to xj causes changes
            if (revise(xi, xj, constraintMap)) {
                // If xi's domain is emptied, no solution is possible
                if (xi.getDomain().isEmpty()) return false;

                // Add all arcs (xk, xi) where xk shares a constraint with xi and xk != xi
                for (Variable xk : constraintMap.keySet()) {
                    if (!xk.equals(xi) && sharesConstraint(xk, xi, constraintMap)) {
                        Pair<Variable, Variable> newArc = new Pair<>(xk, xi);
                        
                        if (seen.add(newArc)) { // Avoid duplicates
                            queue.add(newArc);
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean revise(Variable xi, Variable xj, Map<Variable, List<BinaryConstraint>> constraintMap) {
        boolean revised = false;
        List<TimeSlot> toRemove = new ArrayList<>();
        List<BinaryConstraint> relevantConstraints = constraintMap.getOrDefault(xi, Collections.emptyList());

        List<TimeSlot> domainXi = new ArrayList<>(xi.getDomain());
        List<TimeSlot> domainXj = xj.getDomain();

        for (TimeSlot vi : domainXi) {
            boolean isConsistent = domainXj.stream().anyMatch(vj ->
                relevantConstraints.stream()
                    .filter(c -> c.involves(xi, xj))
                    .allMatch(c -> c.isSatisfied(xi, vi, xj, vj))
            );

            if (!isConsistent) {
                toRemove.add(vi);
                revised = true;
            }
        }

        xi.getDomain().removeAll(toRemove);
        return revised;
    }

    // Overloaded method to run AC3 with domains inside backtracking solver
    public boolean runAC3(List<Variable> variables, List<BinaryConstraint> constraints, Map<Variable, List<TimeSlot>> domains) {
        Queue<Pair<Variable, Variable>> queue = new LinkedList<>();
        Set<Pair<Variable, Variable>> seen = new HashSet<>();
        Map<Variable, List<BinaryConstraint>> constraintMap = buildConstraintMap(variables, constraints);

        for (BinaryConstraint constraint : constraints) {
            List<Variable> involved = constraint.getInvolvedVariables();
            if (involved.size() == 2) {
                Variable vi = involved.get(0);
                Variable vj = involved.get(1);
                if (seen.add(new Pair<>(vi, vj))) queue.add(new Pair<>(vi, vj));
                if (seen.add(new Pair<>(vj, vi))) queue.add(new Pair<>(vj, vi));
            }
        }

        while (!queue.isEmpty()) {
            Pair<Variable, Variable> arc = queue.poll();
            Variable xi = arc.getKey();
            Variable xj = arc.getValue();

            if (revise(xi, xj, constraintMap, domains)) {
                if (domains.get(xi).isEmpty()) return false;

                for (Variable xk : constraintMap.keySet()) {
                    if (!xk.equals(xi) && sharesConstraint(xk, xi, constraintMap)) {
                        Pair<Variable, Variable> newArc = new Pair<>(xk, xi);
                        if (seen.add(newArc)) {
                            queue.add(newArc);
                        }
                    }
                }
            }
        }

        return true;
    }

    // Overloaded method to run AC3 with domains inside backtracking solver
    private boolean revise(Variable xi, Variable xj, Map<Variable, List<BinaryConstraint>> constraintMap, Map<Variable, List<TimeSlot>> domains) {
        boolean revised = false;
        List<BinaryConstraint> relevantConstraints = constraintMap.getOrDefault(xi, List.of());

        List<TimeSlot> domainXi = domains.get(xi);
        List<TimeSlot> domainXj = domains.get(xj);

        List<TimeSlot> toRemove = domainXi.parallelStream()
            .filter(vi -> domainXj.stream().noneMatch(vj ->
                relevantConstraints.stream()
                    .filter(c -> c.involves(xi, xj))
                    .allMatch(c -> c.isSatisfied(xi, vi, xj, vj))
            ))
            .toList();

        if (!toRemove.isEmpty()) {
            domainXi.removeAll(toRemove);
            revised = true;
        }

        return revised;
    }

    private boolean sharesConstraint(Variable a, Variable b, Map<Variable, List<BinaryConstraint>> constraintMap) {
        return constraintMap.getOrDefault(a, Collections.emptyList()).stream()
                .anyMatch(c -> c.involves(a, b));
    }

    private Map<Variable, List<BinaryConstraint>> buildConstraintMap(List<Variable> variables, List<BinaryConstraint> constraints) {
        Map<Variable, List<BinaryConstraint>> map = new ConcurrentHashMap<>();

        // Parallelize over constraints
        constraints.parallelStream().forEach(constraint -> {
            for (Variable var : constraint.getInvolvedVariables()) {
                map.computeIfAbsent(var, k -> Collections.synchronizedList(new ArrayList<>())).add(constraint);
            }
        });

        // Ensure every variable has an entry in the map, even if no constraints
        for (Variable var : variables) {
            map.computeIfAbsent(var, k -> Collections.emptyList());
        }

        return map;
    }
}
