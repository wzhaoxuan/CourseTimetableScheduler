package com.sunway.course.timetable.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.sunway.course.timetable.engine.constraint.interfaces.BinaryConstraint;
import com.sunway.course.timetable.engine.constraint.interfaces.UnaryConstraint;

public class BacktrackingSolver {

    private final AtomicInteger minTotalPenalty = new AtomicInteger(Integer.MAX_VALUE);
    private final Map<Variable, TimeSlot> bestAssignment = new ConcurrentHashMap<>();

    private final ExecutorService executor; // Let caller manage the lifecycle

    public BacktrackingSolver(ExecutorService executor) {
        this.executor = executor;
    }

    public boolean solve(
        List<Variable> variables,
        List<BinaryConstraint> binaryConstraints,
        List<UnaryConstraint> unaryConstraints
    ) throws InterruptedException {
        Variable rootVariable = variables.stream()
            .min(Comparator.comparingInt(v -> v.getDomain().size()))
            .orElse(null);

        if (rootVariable == null) return false;

        // Copy the original domains once, will be used to create fresh local copies per task
        Map<Variable, List<TimeSlot>> originalDomains = initLocalDomains(variables);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (TimeSlot slot : rootVariable.getDomain()) {
            futures.add(executor.submit(() -> {
                Map<Variable, TimeSlot> assignment = new HashMap<>();
                assignment.put(rootVariable, slot);

                // Create a fresh deep copy of domains for this thread/task
                Map<Variable, List<TimeSlot>> localDomains = deepCopyDomains(originalDomains);

                AC3 ac3 = new AC3();
                if (!ac3.runAC3(variables, binaryConstraints, localDomains)) return false;

                // No need to apply domains to shared Variable domains!
                // Pass localDomains along to backtrack so it only works with local copies
                boolean result = backtrack(assignment, variables, binaryConstraints, unaryConstraints, localDomains);

                return result;
            }));
        }

        boolean anySuccess = false;
        for (Future<Boolean> future : futures) {
            try {
                anySuccess |= future.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return anySuccess;
    }

    private boolean backtrack(
        Map<Variable, TimeSlot> assignment,
        List<Variable> variables,
        List<BinaryConstraint> binaryConstraints,
        List<UnaryConstraint> unaryConstraints,
        Map<Variable, List<TimeSlot>> localDomains
    ) {
        System.out.println("Current assignment size: " + assignment.size());
        if (assignment.size() == variables.size()) {
            int totalPenalty = calculateTotalPenalty(assignment, unaryConstraints);
            if (totalPenalty < minTotalPenalty.get()) {
                synchronized (bestAssignment) {
                    if (totalPenalty < minTotalPenalty.get()) {
                        minTotalPenalty.set(totalPenalty);
                        bestAssignment.clear();
                        bestAssignment.putAll(assignment);
                    }
                }
            }
            return true;
        }

        // Find the next unassigned variable with the smallest domain
        // This is a heuristic to improve performance by reducing search space
        Variable unassigned = variables.stream()
            .filter(v -> !assignment.containsKey(v))
            .min(Comparator.comparingInt(v -> localDomains.get(v).size()))
            .orElse(null);

        if (unassigned == null) return false;

        boolean found = false;

        for (TimeSlot slot : unassigned.getDomain()) {
            System.out.printf(" Trying %s for variable %s%n", slot, unassigned.getSession());
            if (!isConsistent(unassigned, slot, assignment, binaryConstraints, unaryConstraints)) {
                System.out.printf(" %s is inconsistent for variable %s%n", slot, unassigned.getSession());
                continue;
            }
            System.out.printf(" %s is consistent for variable %s. Proceeding...%n", slot, unassigned.getSession());

            assignment.put(unassigned, slot);

            // Create a fresh copy of localDomains to simulate domain reduction after assignment
            Map<Variable, List<TimeSlot>> newDomains = deepCopyDomains(localDomains);

            // Remove the assigned slot from the unassigned variable's domain
            newDomains.get(unassigned).clear();

            // Assign the value in the domain map before callling AC3
            newDomains.get(unassigned).add(slot);

            AC3 ac3 = new AC3();
            if (ac3.runAC3(variables, binaryConstraints, newDomains)) {
                boolean result = backtrack(assignment, variables, binaryConstraints, unaryConstraints, newDomains);
                System.out.printf("🔄 Backtracking from variable %s and value %s%n", unassigned.getSession(), slot);
                found = result || found;
            }

            assignment.remove(unassigned);
        }

        return found;
    }

    private boolean isConsistent(
        Variable var,
        TimeSlot value,
        Map<Variable, TimeSlot> assignment,
        List<BinaryConstraint> binaryConstraints,
        List<UnaryConstraint> unaryConstraints
    ) {
        for (UnaryConstraint uc : unaryConstraints) {
            if (uc.isHard() && !uc.isSatisfied(var, value)) return false;
        }

        for (Map.Entry<Variable, TimeSlot> entry : assignment.entrySet()) {
            for (BinaryConstraint bc : binaryConstraints) {
                if (!bc.isSatisfied(var, value, entry.getKey(), entry.getValue())) return false;
            }
        }

        return true;
    }

    private int calculateTotalPenalty(Map<Variable, TimeSlot> assignment, List<UnaryConstraint> constraints) {
        return assignment.entrySet().parallelStream()
        .mapToInt(entry -> constraints.stream()
            .filter(c -> !c.isHard())
            .mapToInt(c -> c.getPenalty(entry.getKey(), entry.getValue()))
            .sum())
        .sum();
    }

    private Map<Variable, List<TimeSlot>> initLocalDomains(List<Variable> variables) {
        Map<Variable, List<TimeSlot>> localDomains = new HashMap<>();
        for (Variable var : variables) {
            localDomains.put(var, new ArrayList<>(var.getDomain()));
        }
        return localDomains;
    }

    // Deep copy utility: copies domain lists for each variable to avoid shared references
    private Map<Variable, List<TimeSlot>> deepCopyDomains(Map<Variable, List<TimeSlot>> original) {
        Map<Variable, List<TimeSlot>> copy = new HashMap<>();
        for (Map.Entry<Variable, List<TimeSlot>> entry : original.entrySet()) {
            // Assuming TimeSlot is immutable or no need to deep copy TimeSlot objects themselves
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public Map<Variable, TimeSlot> getBestAssignment() {
        return bestAssignment;
    }
}
