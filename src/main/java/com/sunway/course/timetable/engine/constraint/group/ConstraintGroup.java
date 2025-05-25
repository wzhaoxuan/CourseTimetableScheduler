// package com.sunway.course.timetable.engine.constraint.group;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;

// import com.sunway.course.timetable.engine.TimeSlot;
// import com.sunway.course.timetable.engine.Variable;
// import com.sunway.course.timetable.engine.constraint.interfaces.BinaryConstraint;

// public class ConstraintGroup implements BinaryConstraint{

//     private final List<BinaryConstraint> constraints;

//     public ConstraintGroup() {
//         this.constraints = new ArrayList<>();
//     }

//     public void addConstraint(BinaryConstraint constraint) {
//         this.constraints.add(constraint);
//     }

//     @Override
//     public boolean isSatisfied(Variable v1, TimeSlot ts1, Variable v2, TimeSlot ts2) {
//         return constraints.stream().allMatch(c -> c.isSatisfied(v1, ts1, v2, ts2));
//     }

//     @Override
//     public boolean involves(Variable v1, Variable v2) {
//         return constraints.stream().anyMatch(c -> c.involves(v1, v2));
//     }

//     @Override
//     public List<Variable> getInvolvedVariables() {
//         Set<Variable> result = new HashSet<>();
//         for (BinaryConstraint constraint : constraints) {
//             result.addAll(constraint.getInvolvedVariables());
//         }
//         return new ArrayList<>(result);
//     }

//     public List<BinaryConstraint> getConstraints() {
//         return constraints;
//     }
// }
