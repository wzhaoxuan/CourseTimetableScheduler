package com.sunway.course.timetable.evaluator;
import java.util.List;

public class EvaluatorResult {

    public record Violation(String name, double weight, double penalty, double score) {}

    private final double percentage;
    private final double totalPenalty;
    private final double maxPenalty;
    private final List<Violation> hardViolations;
    private final List<Violation> softViolations;

    public EvaluatorResult(double percentage, double totalPenalty, double maxPenalty,
                         List<Violation> hardViolations, List<Violation> softViolations) {
        this.percentage = percentage;
        this.totalPenalty = totalPenalty;
        this.maxPenalty = maxPenalty;
        this.hardViolations = hardViolations;
        this.softViolations = softViolations;
    }

    public double getPercentage() {
        return percentage;
    }

    public double getTotalPenalty() {
        return totalPenalty;
    }

    public double getMaxPenalty() {
        return maxPenalty;
    }

    public List<Violation> getHardViolations() {
        return hardViolations;
    }

    public List<Violation> getSoftViolations() {
        return softViolations;
    }
}


