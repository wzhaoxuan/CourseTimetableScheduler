package com.sunway.course.timetable.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sunway.course.timetable.engine.constraint.interfaces.Constraint;
import com.sunway.course.timetable.engine.factory.TimeSlotFactory;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.util.ConstraintGeneratorUtil;


public class ConstraintEngine {
    public List<Session> scheduleSessions(List<Session> unscheduledSessions) {
        List<Variable> variables = new ArrayList<>();
        List<TimeSlot> fullDomain = TimeSlotFactory.generateValidTimeSlots(); // includes filtering for C5, C11
        int count = 0;
        for (Session s : unscheduledSessions) {
            System.out.println(count);
            count++;
            variables.add(new Variable(s, new ArrayList<>(fullDomain)));
        }

        List<Constraint> constraints = new ArrayList<>();
        System.out.println("Generating student clash constraints...");
        constraints.addAll(ConstraintGeneratorUtil.generateStudentClashBinaryConstraints(variables));
        // System.out.println("Generating lecturer clash constraints...");
        // constraints.addAll(ConstraintGeneratorUtil.generateLecturerClashBinaryConstraints(variables));
        // System.out.println("Generating unique type per week constraints...");
        // constraints.addAll(ConstraintGeneratorUtil.generateUniqueTypePerWeekBinaryConstraints(variables));
       
        AC3 ac3 = new AC3();
        System.out.println("AC3 starting");
        System.out.println("Total constraints generated: " + constraints.size());
        boolean consistent = ac3.runAC3(variables, constraints);
        System.out.println("AC3 finished");
        if (!consistent) throw new IllegalStateException("No valid schedule possible under constraints.");

        Map<Variable, TimeSlot> assignment = new HashMap<>();
        BacktrackingSolver solver = new BacktrackingSolver();
        boolean solved = solver.solve(assignment, variables, constraints);
        if (!solved) throw new IllegalStateException("Failed to find a complete schedule.");

        // Apply the result
        for (Map.Entry<Variable, TimeSlot> entry : assignment.entrySet()) {
            Session s = entry.getKey().getSession();
            TimeSlot t = entry.getValue();
            s.setDay(t.getDay().toString());
            s.setStartTime(t.getStartTime());
            s.setEndTime(t.getEndTime());
        }

        return unscheduledSessions;
    }
}
