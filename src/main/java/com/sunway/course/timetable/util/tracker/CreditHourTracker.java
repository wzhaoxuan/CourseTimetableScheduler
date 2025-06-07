package com.sunway.course.timetable.util.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sunway.course.timetable.model.Student;

public class CreditHourTracker {
    private static final int MAX_CREDIT_HOURS = 19;
    private final Map<Student, Integer> studentCreditMap = new HashMap<>();

    public boolean isEligible(Student student, int requiredCredit) {
        return getRemainingCredits(student) >= requiredCredit;
    }

    public void deductCredits(Student student, int credit) {
        int remaining = getRemainingCredits(student);
        studentCreditMap.put(student, remaining - credit);
    }

    public int getRemainingCredits(Student student) {
        return studentCreditMap.getOrDefault(student, MAX_CREDIT_HOURS);
    }

    public List<Student> filterEligible(List<Student> students, int requiredCredit) {
        return students.stream()
                .filter(s -> isEligible(s, requiredCredit))
                .collect(Collectors.toList());
    }

}
