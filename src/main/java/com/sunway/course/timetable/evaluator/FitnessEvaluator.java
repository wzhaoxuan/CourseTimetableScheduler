package com.sunway.course.timetable.evaluator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.evaluator.constraints.hard.DuplicateTypeGroupChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.InvalidDayChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.LecturerClashChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.ModuleClashChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.StudentClashChecker;
import com.sunway.course.timetable.evaluator.constraints.hard.VenueCapacityChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.LateSessionChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.LecturerConsecutiveSessionChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.LongBreakChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.OneSessionDayChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.PracticalBeforeLectureChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.SpreadDaysChecker;
import com.sunway.course.timetable.evaluator.constraints.soft.VenueTransitionChecker;
import com.sunway.course.timetable.model.Satisfaction;
import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.SatisfactionServiceImpl;
import com.sunway.course.timetable.service.venue.VenueDistanceServiceImpl;

@Component
public class FitnessEvaluator {

    private static final Logger log = LoggerFactory.getLogger(FitnessEvaluator.class);

    private final SatisfactionServiceImpl satisfactionService;
    private final List<ConstraintChecker> constraintCheckers;

    public FitnessEvaluator(SatisfactionServiceImpl satisfactionService,
                            VenueDistanceServiceImpl venueDistanceService) {
        this.satisfactionService = satisfactionService;
        this.constraintCheckers = List.of(
            new StudentClashChecker(),
            new LecturerClashChecker(),
            new ModuleClashChecker(),
            new InvalidDayChecker(),
            new VenueCapacityChecker(),
            new DuplicateTypeGroupChecker(),
            new LateSessionChecker(),
            new VenueTransitionChecker(venueDistanceService),
            new OneSessionDayChecker(),
            new LongBreakChecker(),
            new PracticalBeforeLectureChecker(),
            new SpreadDaysChecker(),
            new LecturerConsecutiveSessionChecker()
        );
    }

    public FitnessResult evaluate(List<Session> sessions, Map<Session, Venue> sessionVenueMap, String versionTag) {
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
        percentage = Math.max(0.0, Math.min(100.0, Math.round(percentage * 100.0) / 100.0));

        logFitnessDebug(results, sessions.size(), totalPenalty, maxPenalty, percentage);

        int totalViolations = results.stream().mapToInt(r -> (int) r.penalty()).sum();
        String hash = computeScheduleHash(sessions, sessionVenueMap);

        Satisfaction satisfaction = new Satisfaction(percentage, totalViolations, versionTag);
        satisfaction.setScheduleHash(hash); 
        
        satisfactionService.saveSatisfaction(satisfaction);
        

        List<FitnessResult.Violation> hard = results.stream()
            .filter(r -> r.type() == ConstraintType.HARD)
            .map(r -> new FitnessResult.Violation(r.name(), r.weight(), r.penalty(), r.score()))
            .collect(Collectors.toList());

        List<FitnessResult.Violation> soft = results.stream()
            .filter(r -> r.type() == ConstraintType.SOFT)
            .map(r -> new FitnessResult.Violation(r.name(), r.weight(), r.penalty(), r.score()))
            .collect(Collectors.toList());

        return new FitnessResult(percentage, totalPenalty, maxPenalty, hard, soft);
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
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            List<String> sessionStrings = sessions.stream()
                .map(s -> {
                    Venue v = sessionVenueMap.getOrDefault(s, null);
                    return s.getDay() + "-" + s.getStartTime() + "-" + s.getTypeGroup() + "-" + (v != null ? v.getName() : "");
                })
                .sorted() // sort to ensure consistent hash
                .toList();

            String combined = String.join("|", sessionStrings);
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error computing schedule hash", e);
        }
    }


}



record ConstraintResult(String name, double weight, double penalty, double score, ConstraintType type) {}



