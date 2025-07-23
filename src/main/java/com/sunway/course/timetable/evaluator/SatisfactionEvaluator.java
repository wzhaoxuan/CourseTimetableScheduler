package com.sunway.course.timetable.evaluator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sunway.course.timetable.evaluator.constraints.hard.DuplicateTypeGroupChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.InvalidDayChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.LecturerClashChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.ModuleClashChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.StudentClashChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.VenueCapacityChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.VenueTransitionChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.ConsecutiveSessionChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.LateSessionChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.LongBreakChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.OneSessionDayChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.PracticalBeforeLectureChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.SpreadDaysChecker;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.SatisfactionServiceImpl;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;

@Component
public class SatisfactionEvaluator {

    private final SatisfactionServiceImpl satisfactionService;
    private final List<ConstraintChecker> constraintCheckers;

    public static Set<String> CURRENT_SESSION_KEYS = new HashSet<>();


    /**
     * Constructor to initialize SatisfactionEvaluator with the required services.
     *
     * @param satisfactionService Service for managing satisfaction records
     * @param venueDistanceService Service for managing venue distances
     */
    public SatisfactionEvaluator(SatisfactionServiceImpl satisfactionService,
                            VenueDistanceServiceImpl venueDistanceService) {
        this.satisfactionService = satisfactionService;
        this.constraintCheckers = List.of(
            new StudentClashChecker(),
            new LecturerClashChecker(),
            new ModuleClashChecker(),
            new InvalidDayChecker(),
            new VenueCapacityChecker(),
            new VenueTransitionChecker(venueDistanceService),
            new DuplicateTypeGroupChecker(),
            new LateSessionChecker(),
            new OneSessionDayChecker(),
            new LongBreakChecker(),
            new PracticalBeforeLectureChecker(),
            new SpreadDaysChecker(),
            new ConsecutiveSessionChecker()
        );
    }

    /**
     * Evaluates the fitness of a schedule based on the defined constraints.
     *
     * @param sessions          List of sessions in the schedule
     * @param sessionVenueMap   Map of sessions to their assigned venues
     * @param versionTag        Human-readable version tag for the schedule
     * @return EvaluatorResult containing the evaluation results
     */
    public EvaluatorResult evaluate(List<Session> sessions, Map<Session, Venue> sessionVenueMap, String versionTag) {
        double totalPenalty = 0.0;
        double maxPenalty = 0.0;

        List<ConstraintResult> results = new ArrayList<>();
        for (ConstraintChecker checker : constraintCheckers) {
            double penalty = checker.getPenalty(sessions, sessionVenueMap);
            double score = penalty * checker.getWeight();
            results.add(new ConstraintResult(checker.getName(), checker.getWeight(), penalty, score, checker.getType()));
            maxPenalty += checker.getWeight() * sessions.size();
            totalPenalty += score;
        }

        double percentage = maxPenalty == 0 ? 100.0 : 100.0 * (1.0 - totalPenalty / maxPenalty);
        percentage = Math.max(0.0, Math.min(100.0, Math.round(percentage * 100.0) / 100.0)); // round to 2 decimal places

        logFitnessDebug(results, sessions.size(), totalPenalty, maxPenalty, percentage);

        int totalViolations = results.stream().mapToInt(r -> (int) r.penalty()).sum();
        CURRENT_SESSION_KEYS.clear();
        String hash = computeScheduleHash(sessions, sessionVenueMap);

        Satisfaction sat = new Satisfaction();
        sat.setVersionTag(versionTag);    // store the human‐readable version
        sat.setScheduleHash(hash);        // store the SHA-256
        sat.setScore(percentage);
        sat.setConflict(totalViolations);
        satisfactionService.saveSatisfaction(sat);
        

        List<EvaluatorResult.Violation> hard = results.stream()
            .filter(r -> r.type() == ConstraintType.HARD)
            .map(r -> new EvaluatorResult.Violation(r.name(), r.weight(), r.penalty(), r.score()))
            .collect(Collectors.toList());

        List<EvaluatorResult.Violation> soft = results.stream()
            .filter(r -> r.type() == ConstraintType.SOFT)
            .map(r -> new EvaluatorResult.Violation(r.name(), r.weight(), r.penalty(), r.score()))
            .collect(Collectors.toList());

        return new EvaluatorResult(percentage, totalPenalty, maxPenalty, hard, soft);
    }

    private void logFitnessDebug(List<ConstraintResult> results, int sessionCount, double totalPenalty, double maxPenalty, double fitness) {
        System.out.println("---- FITNESS DEBUG ----");
        System.out.println("Sessions: " + sessionCount);
        for (ConstraintResult r : results) {
            System.out.printf("%s: %.0f\n", r.name(), r.penalty());
        }
        System.out.printf("Total penalty: %.2f\nMax penalty: %.2f\nFinal Fitness %%: %.2f\n",
            totalPenalty, maxPenalty, fitness);
    }

    private String computeScheduleHash(List<Session> sessions, Map<Session, Venue> sessionVenueMap) {
        CURRENT_SESSION_KEYS.clear();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            List<String> sessionStrings = sessions.stream()
                .map(s -> {
                    Venue v = sessionVenueMap.getOrDefault(s, null);
                    return s.getDay() + "-" + s.getStartTime() + "-" + s.getTypeGroup() + "-" + (v != null ? v.getName() : "");
                })
                .sorted() // sort to ensure consistent hash
                .toList();

             // Update current session keys
            //  CURRENT_SESSION_KEYS.clear();
             CURRENT_SESSION_KEYS.addAll(sessionStrings);

            String combined = String.join("|", sessionStrings);
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error computing schedule hash", e);
        }
    }
}


record ConstraintResult(String name, double weight, double penalty, double score, ConstraintType type) {}



