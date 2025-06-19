package com.sunway.course.timetable.exporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.service.PlanContentServiceImpl;
import com.sunway.course.timetable.service.ProgrammeHistoryStorageService;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;
import static com.sunway.course.timetable.util.IntakeUtils.getIntakeLabel;

@Component
public class TimetableExcelExporter {

    private static Logger logger = LoggerFactory.getLogger(TimetableExcelExporter.class);

    private PlanContentServiceImpl planContentService;
    private VenueAssignmentServiceImpl venueAssignmentService;
    private TimetableSheetWriter timetableSheetWriter;
    private ProgrammeHistoryStorageService programmeHistoryStorageService;

    public TimetableExcelExporter(
        PlanContentServiceImpl planContentService,
        VenueAssignmentServiceImpl venueAssignmentService,
        TimetableSheetWriter timetableSheetWriter,
        ProgrammeHistoryStorageService programmeHistoryStorageService) {
        this.planContentService = planContentService;
        this.venueAssignmentService = venueAssignmentService;
        this.timetableSheetWriter = timetableSheetWriter;
        this.programmeHistoryStorageService = programmeHistoryStorageService;
    }

    public List<File> exportWithFitnessAnnotation(
            Map<Integer, Map<String, List<Session>>> sessionBySemesterAndModule,
            double fitnessScore, String programme, String intake, int year) {

        // for(Map.Entry<Integer, Map<String, List<Session>>> entry : sessionBySemesterAndModule.entrySet()) {
        //     logger.info("Semester {}", entry.getKey());
        //     for(Map.Entry<String, List<Session>> moduleEntry : entry.getValue().entrySet()) {
        //         String moduleId = moduleEntry.getKey();
        //         List<Session> sessions = moduleEntry.getValue();
        //         logger.info("Module {} has {} sessions", moduleId, sessions.size());
        //         for(Session session : sessions) {
        //             logger.info("Session: {} {} {} {}",
        //                     session.getDay(), session.getStartTime(), session.getEndTime(), session.getTypeGroup());
        //         }
        //     }
        // }

        // First, collect all semesters involved (sorted)
        List<Integer> allSemesters = sessionBySemesterAndModule.keySet().stream()
                .filter(sem -> sem > 0)
                .sorted()
                .toList();

        // Build mapping: moduleId -> list of semesters it belongs to
        Map<String, Set<Integer>> moduleSemesterMap = new HashMap<>();
        for (var entry : sessionBySemesterAndModule.entrySet()) {
            int semester = entry.getKey();
            for (String moduleId : entry.getValue().keySet()) {
                moduleSemesterMap.computeIfAbsent(moduleId, k -> new HashSet<>()).add(semester);
            }
        }

        List<File> files = new ArrayList<>();

        for (int semester : allSemesters) {
            Map<String, List<Session>> moduleMap = sessionBySemesterAndModule.getOrDefault(semester, Map.of());
            List<Session> combinedSessions = flatten(moduleMap);

            Workbook workbook = timetableSheetWriter.generateWorkbook(
                    "Semester " + semester,
                    groupSessions(combinedSessions),
                    resolveVenueMap(combinedSessions),
                    resolveModuleMap(combinedSessions)
            );

            timetableSheetWriter.addFitnessScore(workbook, fitnessScore);

            File file = saveWorkbookToFile(workbook,
                    String.format("%s-%s S%d.xlsx", programme, getIntakeLabel(semester, intake, year), semester));
            programmeHistoryStorageService.saveProgrammeFile(file);
            files.add(file);
        }

        return files;
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







