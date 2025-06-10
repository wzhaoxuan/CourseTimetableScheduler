package com.sunway.course.timetable.exporter;
import java.util.Objects;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;

@Component
public class TimetableExcelExporter {

    @Autowired private PlanContentServiceImpl planContentService;
    @Autowired private VenueAssignmentServiceImpl venueAssignmentService;

    public List<File> exportWithFitnessAnnotation(
        Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule, 
        double fitnessScore, String programme, String intake, int year) {

        return sessionBySemesterAndModule.entrySet().stream()
            .filter(entry -> entry.getKey() > 0)
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                List<Session> flatSessions = flatten(entry.getValue());
                TimetableSheetWriter writer = new TimetableSheetWriter("Semester " + entry.getKey());
                writer.writeSessions(groupSessions(flatSessions), resolveVenueMap(flatSessions), resolveModuleMap(flatSessions));
                writer.addFitnessScore(fitnessScore);
                try {
                    String fileName = String.format("%s-%s S%d.xlsx", programme, getIntakeLabel(entry.getKey(), intake, year), entry.getKey());
                    return writer.exportToFile(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }


    public List<File> exportLecturerTimetable(Map<Integer, List<Session>> sessionsBySemester, 
                                          String lecturerName, String intake, int year) {
        List<File> files = new ArrayList<>();
        for (var entry : sessionsBySemester.entrySet()) {
            if (entry.getKey() <= 0) continue;
            TimetableSheetWriter writer = new TimetableSheetWriter("Semester " + entry.getKey());
            writer.writeSessions(groupSessions(entry.getValue()), resolveVenueMap(entry.getValue()), resolveModuleMap(entry.getValue()));
            try {
                String fileName = String.format("%s-%s S%d.xlsx", lecturerName, getIntakeLabel(entry.getKey(), intake, year), entry.getKey());
                files.add(writer.exportToFile(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    public Map<Integer, List<Session>> filterSessionsByLecturer(Map<Integer, Map<String, List<Session>>> allSessions, String lecturerName) {
        Map<Integer, List<Session>> lecturerSessions = new HashMap<>();
        for (var entry : allSessions.entrySet()) {
            List<Session> matching = flatten(entry.getValue()).stream()
                .filter(s -> s.getLecturer() != null && s.getLecturer().getName().equalsIgnoreCase(lecturerName))
                .toList();
            if (!matching.isEmpty()) lecturerSessions.put(entry.getKey(), matching);
        }
        return lecturerSessions;
    }

    private List<Session> flatten(Map<String, List<Session>> map) {
        return map.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private Map<String, List<Session>> groupSessions(List<Session> sessions) {
        return sessions.stream().collect(Collectors.groupingBy(
            s -> String.join("|",
                s.getDay(),
                s.getStartTime().toString(),
                s.getTypeGroup(),
                s.getType(),
                s.getLecturer().getName(),
                venueAssignmentService.getVenueBySessionId(s.getId()).map(v -> v.getName()).orElse("Unknown")
            )));
    }

    private Map<Long, String> resolveVenueMap(List<Session> sessions) {
        return sessions.stream()
            .collect(Collectors.toMap(
                Session::getId,
                s -> venueAssignmentService.getVenueBySessionId(s.getId()).map(Venue::getName).orElse("Unknown"),
                (a, b) -> a // merge function to handle duplicates
            ));
    }

    private Map<Long, String> resolveModuleMap(List<Session> sessions) {
        return sessions.stream()
            .collect(Collectors.toMap(
                Session::getId,
                s -> planContentService.getModuleBySessionId(s.getId()).map(p -> p.getModule().getId()).orElse("Unknown"),
                (a, b) -> a
            ));
    }

    private static final List<String> NORMALIZED_INTAKES = List.of("January", "April", "August");

    private String getIntakeLabel(int semester, String userSelectedIntake, int baseYear) {
        String normalized = userSelectedIntake.equalsIgnoreCase("September") ? "August" : userSelectedIntake;
        int baseIndex = NORMALIZED_INTAKES.indexOf(normalized);
        int totalIntakes = NORMALIZED_INTAKES.size();

        int offset = semester - 1;
        int newIndex = (baseIndex - offset % totalIntakes + totalIntakes) % totalIntakes;
        int roundsBack = (offset + (totalIntakes - baseIndex)) / totalIntakes;
        int newYear = baseYear - roundsBack + 1;

        // Use user's original input name if it was "September"
        String displayIntake = (normalized.equals("August") && userSelectedIntake.equalsIgnoreCase("September"))
            ? "September"
            : NORMALIZED_INTAKES.get(newIndex);

        return displayIntake + "-" + newYear;
    }

}






