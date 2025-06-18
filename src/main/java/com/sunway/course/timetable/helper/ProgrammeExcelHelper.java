package com.sunway.course.timetable.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.sunway.course.timetable.model.Student;


public class ProgrammeExcelHelper {

    private static final List<String> INTAKES = Arrays.asList(
        "January", "April", "September"
    );

    public static Map<String, List<Student>> assignStudentsToProgrammesEqually(List<Student> students) {
        Map<String, List<Student>> grouped = new HashMap<>();
        List<String> programmeIds = List.of("BIT", "BCS", "BSE", "BCNS", "BSDA");

        for (int i = 0; i < students.size(); i++) {
            String progId = programmeIds.get(i % programmeIds.size());
            grouped.computeIfAbsent(progId, k -> new ArrayList<>()).add(students.get(i));
        }
        return grouped;
    }

    public static String assignProgrammeName(String programmeId) {
        return switch (programmeId) {
            case "BIT" -> "Bachelor of Information Technology";
            case "BCS" -> "Bachelor of Computer Science";
            case "BSE" -> "Bachelor of Software Engineering";
            case "BCNS" -> "Bachelor of Computer Network Security";
            case "BSDA" -> "Bachelor of Data Analytics";
            default -> "";
        };
    }

    public static String getRandomIntake() {
        return INTAKES.get(new Random().nextInt(INTAKES.size())).toUpperCase();
    }
}
