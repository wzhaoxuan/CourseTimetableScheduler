package com.sunway.course.timetable.exporter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.plan.Plan;
import com.sunway.course.timetable.service.PlanServiceImpl;
import com.sunway.course.timetable.util.IntakeUtils;


@Component
public class HistoricalTimetableExporter {

    private final PlanServiceImpl planService;
    private final TimetableSheetWriter timetableSheetWriter;
    private final Map<String, ModuleAssignmentData> moduleDataMap;
    private final Map<Long, Integer> studentSemesterMap;

    public HistoricalTimetableExporter(
            PlanServiceImpl planService,
            TimetableSheetWriter timetableSheetWriter,
            Map<String, ModuleAssignmentData> moduleDataMap,
            Map<Long, Integer> studentSemesterMap) {
        this.planService = planService;
        this.timetableSheetWriter = timetableSheetWriter;
        this.moduleDataMap = moduleDataMap;
        this.studentSemesterMap = studentSemesterMap;
    }

    // ✅ Export for Programme
    public List<File> exportByProgramme(String programme, String intake, int year) {
        List<Plan> plans = planService.getPlansByProgramme(programme, moduleDataMap);
        return generateTimetableFiles(plans, programme, intake, year);
    }

    // ✅ Export for Lecturer
    public List<File> exportByLecturer(String lecturerName) {
        List<Plan> plans = planService.getPlansByLecturer(lecturerName);
        return generateTimetableFiles(plans, lecturerName, null, 0);
    }

    // ✅ Export for Module
    public List<File> exportByModule(String moduleId) {
        List<Plan> plans = planService.getPlansByModule(moduleId);
        return generateTimetableFiles(plans, moduleId, null, 0);
    }

    // ✅ Group by semester -> Generate timetable files
    private List<File> generateTimetableFiles(List<Plan> plans, String identifier, String intake, int year) {

        Map<Integer, List<Plan>> plansBySemester = plans.stream()
            .collect(Collectors.groupingBy(plan -> {
                Long studentId = plan.getPlanContent().getSession().getStudent().getId();
                return studentSemesterMap.getOrDefault(studentId, 0);
            }));

        List<File> files = new ArrayList<>();

        for (Map.Entry<Integer, List<Plan>> entry : plansBySemester.entrySet()) {
            int semester = entry.getKey();

            // Build file name
            String fileName;
            if (intake != null) {
                String intakeLabel = IntakeUtils.getIntakeLabel(semester, intake, year);
                fileName = String.format("%s-%s S%d.xlsx", identifier, intakeLabel, semester);
            } else{
                fileName = String.format("%s.xlsx", identifier);
            }

            // DEDUPLICATION STEP
            Set<Long> seenSessionIds = new HashSet<>();
            List<Session> uniqueSessions = entry.getValue().stream()
                .map(plan -> plan.getPlanContent().getSession())
                .filter(s -> seenSessionIds.add(s.getId()))
                .collect(Collectors.toList());      

            // ✅ Reuse TimetableSheetWriter now
            Workbook workbook = timetableSheetWriter.generateWorkbookFromSessions("Semester " + semester, uniqueSessions);
            File file = saveWorkbookToFile(workbook, fileName);
            files.add(file);
        }
        return files;
    }

    // ✅ Get all distinct lecturers (used for displaying list of buttons in LecturerController)
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



