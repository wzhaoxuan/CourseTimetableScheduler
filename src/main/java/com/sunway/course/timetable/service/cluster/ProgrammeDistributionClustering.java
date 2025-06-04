package com.sunway.course.timetable.service.cluster;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;

@Service
public class ProgrammeDistributionClustering {

    /**
     * Calculates percentage distribution of students' programmes per module per semester.
     *
     * @param allMetaData List of session group metadata containing module and semester information
     * @param moduleIdToStudentsMap Map of module ID to list of students enrolled in that module
     * @param studentProgrammeMap Map of student ID to their programme
     * @param studentSemesterMap Map of student ID to their semester
     * @return Map where keys are semesters, and values are maps:
     *         moduleCode -> (programme -> percentage)
     */
    public ProgrammeDistributionResult clusterProgrammeDistribution(
        List<SessionGroupMetaData> allMetaData,
        Map<String, List<Student>> moduleIdToStudentsMap,
        Map<Long, String> studentProgrammeMap,
        Map<Long, Integer> studentSemesterMap
    ) {
        Map<Integer, Map<String, Map<String, Integer>>> semesterModuleProgrammeCounts = new HashMap<>();
        Map<Integer, Map<String, Integer>> semesterModuleTotalStudents = new HashMap<>();
        Map<SessionGroupMetaData, String> majorityProgrammeByGroup = new HashMap<>();

        for (SessionGroupMetaData meta : allMetaData) {
            int semester = meta.getSemester();
            String moduleId = meta.getModuleId();

            List<Student> students = moduleIdToStudentsMap.getOrDefault(moduleId, Collections.emptyList());
            Map<String, Integer> localCounts = new HashMap<>();

            for (Student student : students) {
                Long studentId = student.getId();
                String programme = studentProgrammeMap.get(studentId);
                Integer studentSem = studentSemesterMap.get(studentId);

                if (programme == null || studentSem == null || studentSem != semester) continue;
                if (!meta.getType().equalsIgnoreCase("Lecture")) continue;

                localCounts.merge(programme, 1, Integer::sum);

                semesterModuleProgrammeCounts
                    .computeIfAbsent(semester, k -> new HashMap<>())
                    .computeIfAbsent(moduleId, k -> new HashMap<>())
                    .merge(programme, 1, Integer::sum);

                semesterModuleTotalStudents
                    .computeIfAbsent(semester, k -> new HashMap<>())
                    .merge(moduleId, 1, Integer::sum);
            }

            String majorityProgramme = localCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");

            majorityProgrammeByGroup.put(meta, majorityProgramme);
        }

        Map<Integer, Map<String, Map<String, Double>>> result = new HashMap<>();
        for (var semEntry : semesterModuleProgrammeCounts.entrySet()) {
            int semester = semEntry.getKey();
            Map<String, Map<String, Integer>> moduleCounts = semEntry.getValue();
            Map<String, Integer> totalCounts = semesterModuleTotalStudents.get(semester);
            Map<String, Map<String, Double>> modulePercentages = new HashMap<>();

            for (var modEntry : moduleCounts.entrySet()) {
                String moduleId = modEntry.getKey();
                Map<String, Integer> progCounts = modEntry.getValue();
                int total = totalCounts.getOrDefault(moduleId, 0);

                Map<String, Double> percentMap = new HashMap<>();
                for (var p : progCounts.entrySet()) {
                    percentMap.put(p.getKey(), (p.getValue() * 100.0) / total);
                }

                modulePercentages.put(moduleId, percentMap);
            }

            result.put(semester, modulePercentages);
        }

        return new ProgrammeDistributionResult(result, majorityProgrammeByGroup);
    }
}
