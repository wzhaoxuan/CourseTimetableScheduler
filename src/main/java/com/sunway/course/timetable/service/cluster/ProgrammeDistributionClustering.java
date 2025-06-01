package com.sunway.course.timetable.service.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Session;
import com.sunway.course.timetable.model.Student;

@Service
public class ProgrammeDistributionClustering {

    /**
     * Calculates percentage distribution of students' programmes per module per semester.
     *
     * @param sessions List of sessions with assigned students
     * @param studentProgrammeMap Map of student ID to their programme
     * @param studentSemesterMap Map of student ID to their semester
     * @return Map where keys are semesters, and values are maps:
     *         moduleCode -> (programme -> percentage)
     */
    public Map<Integer, Map<String, Map<String, Double>>> calculateProgrammePercentageDistributionBySemester(
            List<Session> sessions,
            Map<Long, String> studentProgrammeMap,
            Map<Long, Integer> studentSemesterMap
    ) {
        // Intermediate count maps
        Map<Integer, Map<String, Map<String, Integer>>> semesterModuleProgrammeCounts = new HashMap<>();
        Map<Integer, Map<String, Integer>> semesterModuleTotalStudents = new HashMap<>();

        for (Session session : sessions) {
            Student student = session.getStudent();
            if (student == null) continue;

            Long studentId = student.getId();
            String programme = studentProgrammeMap.get(studentId);
            Integer semester = studentSemesterMap.get(studentId);
            if (programme == null || semester == null) continue;

            String typeGroup = session.getTypeGroup();
            if(typeGroup == null || !typeGroup.toLowerCase().contains("lecture")) continue;

            String moduleCode = extractModuleCodeFromGroup(typeGroup);
            if (moduleCode == null) continue;

            // Initialize nested maps
            semesterModuleProgrammeCounts
                    .computeIfAbsent(semester, k -> new HashMap<>())
                    .computeIfAbsent(moduleCode, k -> new HashMap<>())
                    .merge(programme, 1, Integer::sum);
            
            semesterModuleTotalStudents
                    .computeIfAbsent(semester, k -> new HashMap<>())
                    .merge(moduleCode, 1, Integer::sum);
        }

        // Final percentage map
        Map<Integer, Map<String, Map<String, Double>>> result = new HashMap<>();

        for (Map.Entry<Integer, Map<String, Map<String, Integer>>> semesterEntry : semesterModuleProgrammeCounts.entrySet()) {
            Integer semester = semesterEntry.getKey();
            Map<String, Map<String, Integer>> moduleProgrammeCounts = semesterEntry.getValue();
            Map<String, Integer> moduleTotalCounts = semesterModuleTotalStudents.get(semester);
            Map<String, Map<String, Double>> modulePercentages = new HashMap<>();

            for (Map.Entry<String, Map<String, Integer>> moduleEntry : moduleProgrammeCounts.entrySet()) {
                String moduleCode = moduleEntry.getKey();
                Map<String, Integer> programmeCounts = moduleEntry.getValue();
                int total = moduleTotalCounts.getOrDefault(moduleCode, 0);
                Map<String, Double> percentageMap = new HashMap<>();

                for (Map.Entry<String, Integer> entry : programmeCounts.entrySet()) {
                    String programme = entry.getKey();
                    int count = entry.getValue();
                    double percentage = (count * 100.0) / total;
                    percentageMap.put(programme, percentage);
                }
                // System.out.println("-----------------------------------------");

                modulePercentages.put(moduleCode, percentageMap);
            }

            result.put(semester, modulePercentages);
        }

        return result;
    }

    // Helper to extract module code from "CODE-..." string
    private String extractModuleCodeFromGroup(String typeGroup) {
        if (typeGroup == null || typeGroup.isEmpty()) return null;
        int dashIndex = typeGroup.indexOf('-');
        if (dashIndex == -1) return null;
        return typeGroup.substring(0, dashIndex).trim();
    }

}
