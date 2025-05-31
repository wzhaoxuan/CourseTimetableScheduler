package com.sunway.course.timetable.engine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.engine.constraint.interfaces.BinaryConstraint;
import com.sunway.course.timetable.engine.constraint.interfaces.UnaryConstraint;
import com.sunway.course.timetable.engine.constraint.soft.LecturerUnavailabilityConstraint;
import com.sunway.course.timetable.engine.factory.TimeSlotFactory;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.repository.VenueRepository;
import com.sunway.course.timetable.repository.VenueDistanceRepository;
import com.sunway.course.timetable.store.WeekdaySessionStore;
import com.sunway.course.timetable.util.ConstraintGeneratorUtil;

@Component
public class ConstraintEngine {

    private final WeekdaySessionStore weekdaySessionStore;
    private final VenueRepository venueRepository; // Assuming this is injected or available
    private final VenueDistanceRepository venueDistanceRepository; // Assuming this is injected or available
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public ConstraintEngine(WeekdaySessionStore weekdaySessionStore, 
                            VenueRepository venueRepository,
                            VenueDistanceRepository venueDistanceRepository) {
        this.weekdaySessionStore = weekdaySessionStore;
        this.venueRepository = venueRepository;
        this.venueDistanceRepository = venueDistanceRepository;
    }

    public List<Session> scheduleSessions(List<Session> unscheduledSessions) {
        // Step 1: Create variables from sessions
        List<Variable> variables = new ArrayList<>();

        List<Venue> allVenues = venueRepository.findAll();

        // List<TimeSlot> fullDomain = TimeSlotFactory.generateTimeSlotsByVenueDistance(); // includes filtering for C5, C11
        // for (Session s : unscheduledSessions) {
        //     variables.add(new Variable(s, new ArrayList<>(fullDomain)));
        // }

        // Step 2: Generate binary constraints
        List<BinaryConstraint> binaryConstraints = new ArrayList<>();
        System.out.println("Generating student clash binary constraints...");
        binaryConstraints.addAll(ConstraintGeneratorUtil.generateStudentClashBinaryConstraints(variables));
        // System.out.println("Generating lecturer clash binaryConstraints...");
        // binaryConstraints.addAll(ConstraintGeneratorUtil.generateLecturerClashBinarybinaryConstraints(variables));
        // System.out.println("Generating unique type per week binaryConstraints...");
        // binaryConstraints.addAll(ConstraintGeneratorUtil.generateUniqueTypePerWeekBinarybinaryConstraints(variables));

        // Step 3: Generate unary constraints (e.g. soft lecturer unavailability)
        List<UnaryConstraint> unaryConstraints = new ArrayList<>();
        Map<Long, Set<String>> unavailableDays = weekdaySessionStore.getAllAvailability();

        for(Map.Entry<Long, Set<String>> entry : unavailableDays.entrySet()) {
            Long lecturerId = entry.getKey();
            Set<String> days = entry.getValue();
            System.out.println("Lecturer ID: " + lecturerId + ", Unavailable Days: " + days);
            }
        
        unaryConstraints.add(new LecturerUnavailabilityConstraint(unavailableDays));

       // Step 4: Apply AC3 to reduce domains using only hard binary constraints
        AC3 ac3 = new AC3();
        System.out.println("AC3 starting");
        System.out.println("Total binaryConstraints generated: " + binaryConstraints.size());
        boolean consistent = ac3.runAC3(variables, binaryConstraints);
        System.out.println("AC3 finished");

        if (!consistent) throw new IllegalStateException("No valid schedule possible under binaryConstraints.");

        // Step 5: Solve using Backtracking considering both binary and unary constraints
        BacktrackingSolver solver = new BacktrackingSolver(executor);
        boolean solved = false;
        try {
            solved = solver.solve(variables, binaryConstraints, unaryConstraints);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore interrupted status
            System.err.println("Solving interrupted: " + e.getMessage());
        }

        Map<Variable, TimeSlot> assignment = solver.getBestAssignment();
        executor.shutdown();

        // Step 6: Apply result to session entities
        for (Map.Entry<Variable, TimeSlot> entry : assignment.entrySet()) {
            Session s = entry.getKey().getSession();
            TimeSlot t = entry.getValue();
            s.setDay(t.getDay().toString());
            s.setStartTime(t.getStartTime());
            s.setEndTime(t.getEndTime());
        }

        if (solved) {
            System.out.println("Timetable successfully generated:");
            assignment.forEach((variable, timeslot) -> {
                System.out.printf("Session: %-25s | Day: %-8s | Start: %-5s | End: %-5s%n",
                    variable.getSession(),
                    timeslot.getDay(),
                    timeslot.getStartTime(),
                    timeslot.getEndTime()
                );
            });
        } else {
            System.out.println("Failed to generate a valid timetable.");
        }


        return unscheduledSessions;
    }
}
