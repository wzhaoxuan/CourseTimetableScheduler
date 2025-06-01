package com.sunway.course.timetable.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sunway.course.timetable.engine.Variable;
import com.sunway.course.timetable.engine.constraint.hard.LecturerClashConstraint;
import com.sunway.course.timetable.engine.constraint.hard.StudentClashConstraint;
import com.sunway.course.timetable.engine.constraint.hard.UniqueTypePerWeekConstraint;
import com.sunway.course.timetable.engine.constraint.interfaces.BinaryConstraint;

import javafx.util.Pair;

public class ConstraintGeneratorUtil {

    public static List<BinaryConstraint> generateStudentClashBinaryConstraints(List<Variable> variables) {
        Set<Pair<Variable, Variable>> seenPairs = ConcurrentHashMap.newKeySet();

    return IntStream.range(0, variables.size())
        .parallel()
        .boxed()
        .flatMap(i -> {
            Variable v1 = variables.get(i);
            return IntStream.range(i + 1, variables.size())
                .mapToObj(j -> {
                    Variable v2 = variables.get(j);

                    if (v1.getSession().getStudent().equals(v2.getSession().getStudent())) {
                        Pair<Variable, Variable> ordered = orderedPair(v1, v2);
                        if (seenPairs.add(ordered)) {
                            return new StudentClashConstraint(v1, v2);
                        }
                    }
                    return null;
                });
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    }

    private static Pair<Variable, Variable> orderedPair(Variable a, Variable b) {
        return a.hashCode() <= b.hashCode() ? new Pair<>(a, b) : new Pair<>(b, a);
    }


    public static List<BinaryConstraint> generateLecturerClashBinaryConstraints(List<Variable> variables) {
        List<BinaryConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            for (int j = i + 1; j < variables.size(); j++) {
                Variable v1 = variables.get(i);
                Variable v2 = variables.get(j);

                if (v1.getSession() == null || v2.getSession() == null) continue;
                if (v1.getSession().getLecturer() == null || v2.getSession().getLecturer() == null) continue;

                if (Objects.equals(v1.getSession().getLecturer(), v2.getSession().getLecturer())) {
                    constraints.add(new LecturerClashConstraint(v1, v2));
                }
            }
        }
        return constraints;
    }

    public static List<BinaryConstraint> generateUniqueTypePerWeekBinaryConstraints(List<Variable> variables) {
        List<BinaryConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            for (int j = i + 1; j < variables.size(); j++) {
                Variable v1 = variables.get(i);
                Variable v2 = variables.get(j);

                if (v1.getSession() == null || v2.getSession() == null) continue;
                if (v1.getSession().getType() == null || v2.getSession().getType() == null) continue;
                if (v1.getSession().getTypeGroup() == null || v2.getSession().getTypeGroup() == null) continue;

                if (v1.getSession().getType().equals(v2.getSession().getType())
                        && v1.getSession().getTypeGroup().equals(v2.getSession().getTypeGroup())) {
                    constraints.add(new UniqueTypePerWeekConstraint(v1, v2));
                }
            }
        }
        return constraints;
    }


    // Optionally, add similar methods for LecturerClashBinaryConstraint etc.
}
