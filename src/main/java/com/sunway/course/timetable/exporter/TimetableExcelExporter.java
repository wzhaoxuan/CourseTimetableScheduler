package com.sunway.course.timetable.exporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;
import static com.sunway.course.timetable.util.IntakeUtils.getIntakeLabel;

@Component
public class TimetableExcelExporter {

    @Autowired private PlanContentServiceImpl planContentService;
    @Autowired private VenueAssignmentServiceImpl venueAssignmentService;
    @Autowired private TimetableSheetWriter timetableSheetWriter;

    public List<File> exportWithFitnessAnnotation(
        Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule,
        double fitnessScore, String programme, String intake, int year) {

        return sessionBySemesterAndModule.entrySet().stream()
            .filter(entry -> entry.getKey() > 0)
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                int semester = entry.getKey();
                List<Session> sessions = flatten(entry.getValue());
                List<Session> uniqueSessions = deduplicateSessions(sessions);

                Map<String, List<Session>> grouped = groupSessions(uniqueSessions);
                Map<Long, String> venueMap = resolveVenueMap(uniqueSessions);
                Map<Long, String> moduleMap = resolveModuleMap(uniqueSessions);

                Workbook workbook = timetableSheetWriter.generateWorkbook("Semester " + semester, grouped, venueMap, moduleMap);
                timetableSheetWriter.addFitnessScore(workbook, fitnessScore);

                return saveWorkbookToFile(workbook, String.format("%s-%s S%d.xlsx", programme, getIntakeLabel(semester, intake, year), semester));
            })
            .collect(Collectors.toList());
    }

    public List<File> exportLecturerTimetable(Map<Integer, List<Session>> sessionsBySemester,
                                               String lecturerName, String intake, int year) {
        List<File> files = new ArrayList<>();
        for (var entry : sessionsBySemester.entrySet()) {
            int semester = entry.getKey();
            List<Session> sessions = deduplicateSessions(entry.getValue());
            if(semester <= 0 || sessions.isEmpty()) continue;
            Map<Long, String> venueMap = resolveVenueMap(sessions);
            Map<Long, String> moduleMap = resolveModuleMap(sessions);
            

            Workbook workbook = timetableSheetWriter.generateWorkbookSimple("Semester " + semester, sessions, venueMap, moduleMap);
            String fileName = String.format("%s-%s S%d.xlsx", lecturerName, getIntakeLabel(semester, intake, year), semester);
            files.add(saveWorkbookToFile(workbook, fileName));
        }
        return files;
    }

    public List<File> exportModuleTimetable(
        Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule,
        String intake, int year) {

        List<File> files = new ArrayList<>();

        for (var semEntry : sessionBySemesterAndModule.entrySet()) {
            int semester = semEntry.getKey();
            Map<String, List<Session>> moduleMap = semEntry.getValue();
            if(semester <= 0) continue;

            for (var moduleEntry : moduleMap.entrySet()) {
                String moduleId = moduleEntry.getKey();
                List<Session> sessions = deduplicateSessions(moduleEntry.getValue());
                if (sessions.isEmpty()) continue;

                // Group directly: no need to re-group using complex keys
                Map<Long, String> venueMap = resolveVenueMap(sessions);
                Map<Long, String> moduleCodeMap = resolveModuleMap(sessions);

                // We simply write all sessions for this module directly:
                Workbook workbook = timetableSheetWriter.generateWorkbookSimple("Semester " + semester, sessions, venueMap, moduleCodeMap);
                String fileName = String.format("%s-%s S%d.xlsx", moduleId, getIntakeLabel(semester, intake, year), semester);
                files.add(saveWorkbookToFile(workbook, fileName));
            }
        }

        return files;
    }


    public Map<Integer, List<Session>> filterSessionsByLecturer(
        Map<Integer, Map<String, List<Session>>> allSessions, String lecturerName) {
        
        Map<Integer, List<Session>> lecturerSessions = new HashMap<>();
        for (var entry : allSessions.entrySet()) {
            List<Session> matching = flatten(entry.getValue()).stream()
                .filter(s -> s.getLecturer() != null && s.getLecturer().getName().equalsIgnoreCase(lecturerName))
                .toList();
            if (!matching.isEmpty()) lecturerSessions.put(entry.getKey(), matching);
        }
        return lecturerSessions;
    }

    private List<Session> deduplicateSessions(List<Session> sessions) {
        return sessions.stream()
                .collect(Collectors.toMap(
                        s -> s.getDay() + "|" + s.getStartTime() + "|" + s.getTypeGroup(),
                        Function.identity(),
                        (s1, s2) -> s1  // keep first
                ))
                .values().stream().toList();
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
        return sessions.stream().collect(Collectors.toMap(
            Session::getId,
            s -> venueAssignmentService.getVenueBySessionId(s.getId()).map(Venue::getName).orElse("Unknown"),
            (a, b) -> a
        ));
    }

    private Map<Long, String> resolveModuleMap(List<Session> sessions) {
        return sessions.stream().collect(Collectors.toMap(
            Session::getId,
            s -> planContentService.getModuleBySessionId(s.getId()).map(p -> p.getModule().getId()).orElse("Unknown"),
            (a, b) -> a
        ));
    }

    private File saveWorkbookToFile(Workbook workbook, String filename) {
        File file = new File(System.getProperty("user.home") + "/Downloads/" + filename);
        try (FileOutputStream out = new FileOutputStream(file)) {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}







