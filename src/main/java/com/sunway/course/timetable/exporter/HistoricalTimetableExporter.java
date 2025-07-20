package com.sunway.course.timetable.exporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Venue;
import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.model.venueAssignment.VenueAssignment;
import com.sunway.course.timetable.service.PlanServiceImpl;
import com.sunway.course.timetable.service.ProgrammeHistoryStorageService;
import com.sunway.course.timetable.service.venue.VenueAssignmentServiceImpl;
import com.sunway.course.timetable.util.IntakeUtils;


@Component
public class HistoricalTimetableExporter {

    private final PlanServiceImpl planService;
    private final VenueAssignmentServiceImpl venueAssignmentService;
    private final TimetableSheetWriter timetableSheetWriter;
    private final Map<Long, Integer> studentSemesterMap;
    private final ProgrammeHistoryStorageService programmeHistoryStorageService;

    public HistoricalTimetableExporter(
            PlanServiceImpl planService,
            VenueAssignmentServiceImpl venueAssignmentService,
            TimetableSheetWriter timetableSheetWriter,
            Map<Long, Integer> studentSemesterMap,
            ProgrammeHistoryStorageService programmeHistoryStorageService) {
        this.planService = planService;
        this.venueAssignmentService = venueAssignmentService;
        this.timetableSheetWriter = timetableSheetWriter;
        this.studentSemesterMap = studentSemesterMap;
        this.programmeHistoryStorageService = programmeHistoryStorageService;
    }

    public List<File> exportByProgramme(String programmeCode) {
        File[] files = programmeHistoryStorageService.getProgrammeFilesForCode(programmeCode);
        if (files == null || files.length == 0) {
            return List.of();
        }
        return Arrays.asList(files);
    }

    public List<File> exportByLecturer(String lecturerName, String versionTag) {
        List<Plan> plans = planService.getPlansByLecturerAndVersion(lecturerName, versionTag);
        return generateTimetableFiles(plans, lecturerName, null, 0, versionTag);
    }


    public List<File> exportByModule(String moduleId, String versionTag) {
        List<Plan> plans = planService.getPlansByModuleAndVersion(moduleId, versionTag);
        return generateTimetableFiles(plans, moduleId, null, 0,versionTag);
    }

    private List<File> generateTimetableFiles(List<Plan> plans, String identifier, String intake, int year, String versionTag) {
        // 1) Group by semester
        Map<Integer, List<Plan>> plansBySemester = plans.stream()
            .collect(Collectors.groupingBy(plan -> {
                Long studentId = plan.getPlanContent().getSession().getStudent().getId();
                return studentSemesterMap.getOrDefault(studentId, 0);
            }));

         // BEFORE the per-semester loop, build one map of Session-ID → VenueName for this version:
        // 2) Pull out distinct session IDs
        List<Long> sessionIds = plans.stream()
            .map(plan -> plan.getPlanContent().getSession().getId())
            .distinct()
            .toList();

         // 3) Build a version-scoped venue map
        Map<Long,String> versionVenueMap = sessionIds.stream()
            .collect(Collectors.toMap(
                Function.identity(), // key = sessionId
                id -> venueAssignmentService.getAssignmentBySessionIdAndVersionTag(id, versionTag)
                        .map(VenueAssignment::getVenue)
                        .map(Venue::getName)
                        .orElse("Unknown")
            ));

        List<File> files = new ArrayList<>();

        for (Map.Entry<Integer, List<Plan>> entry : plansBySemester.entrySet()) {
            int semester = entry.getKey();

            String fileName = (intake != null)
                ? String.format("%s-%s S%d-%s.xlsx", identifier, IntakeUtils.getIntakeLabel(semester, intake, year), semester, versionTag)
                : String.format("%s-%s.xlsx", identifier, versionTag);

            // DEDUPLICATE properly:
            List<Session> uniqueSessions = entry.getValue().stream()
                .map(plan -> plan.getPlanContent().getSession())
                .toList();

            List<Session> deduplicated = deduplicateSessions(uniqueSessions);

            Workbook workbook = timetableSheetWriter.generateWorkbookFromSessions("Semester " + semester, deduplicated, versionVenueMap);
            File file = saveWorkbookToFile(workbook, fileName);
            files.add(file);
        }
        return files;
    }

    public List<String> getAllLecturerNames() {
        return planService.getAllPlans().stream()
            .map(plan -> plan.getPlanContent().getSession().getLecturer().getName())
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();
    }

    public List<String> getAllModuleIds() {
        return planService.getAllPlans().stream()
            .map(plan -> plan.getPlanContent().getModule().getId())
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();
    }

    // Deduplicate function
    private List<Session> deduplicateSessions(List<Session> sessions) {
        return sessions.stream()
                .collect(Collectors.toMap(
                        s -> s.getDay() + "|" + s.getStartTime() + "|" + s.getTypeGroup(),
                        Function.identity(),
                        (s1, s2) -> s1  // keep first
                ))
                .values().stream().toList();
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




