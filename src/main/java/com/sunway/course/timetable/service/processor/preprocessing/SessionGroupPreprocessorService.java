package com.sunway.course.timetable.service.processor.preprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.sunway.course.timetable.model.Module;
import com.sunway.course.timetable.model.Student;
import com.sunway.course.timetable.model.SubjectPlanInfo;
import com.sunway.course.timetable.model.assignment.ModuleAssignmentData;
import com.sunway.course.timetable.model.assignment.SessionGroupMetaData;
import com.sunway.course.timetable.util.SchedulingUtils;

@Service
public class SessionGroupPreprocessorService {
    private final int MAX_GROUP_SIZE = 35;
    private final Map<String, Set<Integer>> moduleSemesterMap = new HashMap<>();
    private final Map<String, Integer> lecturerHourMap = new HashMap<>();

    /**
     * Prepare session group metadata from raw sessions.
     * Groups sessions by semester, module, and type+group and generates metadata.
     * 
     * @param data ModuleAssignmentData containing module info and eligible students
     * @param studentSemesterMap Mapping of student ID to semester
     * @return List of SessionGroupMetaData, one per group for scheduling
     */
    public List<SessionGroupMetaData> prepareSessionGroupMetadata(ModuleAssignmentData data, Map<Long, Integer> studentSemesterMap) {
        
        Module module = data.getModule();
        SubjectPlanInfo plan = data.getSubjectPlanInfo();
        Set<Student> allStudents = data.getEligibleStudents();

        // === GROUP STUDENTS BY SEMESTER ===
        Map<Integer, List<Student>> studentsBySemester = new HashMap<>();
        for (Student student : allStudents) {
            Integer semester = studentSemesterMap.get(student.getId());
            if (semester != null) {
                studentsBySemester.computeIfAbsent(semester, k -> new ArrayList<>()).add(student);
            }
        }

        for (Map.Entry<Integer, List<Student>> entry : studentsBySemester.entrySet()) {
            int semester = entry.getKey();
            moduleSemesterMap.computeIfAbsent(module.getId(), k -> new HashSet<>()).add(semester);
        }


        List<SessionGroupMetaData> metaDataList = new ArrayList<>();

        // === 1. CREATE SINGLE LECTURE (for combined semesters) ===
        if (plan.hasLecture()) {
            String lecturer = plan.getMainLecturer();
            lecturerHourMap.merge(lecturer, 2, Integer::sum);  // 2 hours for lecture
            SchedulingUtils.resetTeachingHours(lecturerHourMap);
            SchedulingUtils.recordTeachingHours(lecturerHourMap, lecturer, 2);

            String lectureGroup = plan.getSubjectCode() + "-Lecture-G1";
            List<Student> allStudentsCombined = allStudents.stream().toList();


            SessionGroupMetaData lectureMeta = new SessionGroupMetaData(
                0, // semester irrelevant for lecture
                module.getId(),
                "Lecture",
                lectureGroup,
                plan.getMainLecturer(),
                allStudentsCombined.size(),
                0,
                1,
                allStudentsCombined
            );
            metaDataList.add(lectureMeta);
        }

        // === 2. CREATE PRACTICAL / TUTORIAL / WORKSHOP GROUPS PER SEMESTER ===
        List<SessionTypeInfo> sessionTypes = List.of(
            new SessionTypeInfo("Tutorial", plan.hasTutorial(), plan.getTutorialTutor()),
            new SessionTypeInfo("Practical", plan.hasPractical(), plan.getPracticalTutor()),
            new SessionTypeInfo("Workshop", plan.hasWorkshop(), plan.getWorkshopTutor())
        );

        List<Student> allStudentsSorted = new ArrayList<>(allStudents);
        // allStudentsSorted.sort(Comparator.comparingLong(Student::getId));  // optional: for deterministic ordering

        int totalStudents = allStudentsSorted.size();
        int groupCount = (int) Math.ceil((double) totalStudents / MAX_GROUP_SIZE);

        for (SessionTypeInfo typeInfo : sessionTypes) {
            if (!typeInfo.hasType) continue;
            List<String> tutors = typeInfo.tutor();

            for (int i = 0; i < groupCount; i++) {
                String groupName = plan.getSubjectCode() + "-" + typeInfo.type() + "-G" + (i + 1);
                String tutor = tutors.isEmpty() ? null : tutors.get(i % tutors.size());
                if (tutor != null) {
                    lecturerHourMap.merge(tutor, 2, Integer::sum); // 2 hours per session
                    SchedulingUtils.resetTeachingHours(lecturerHourMap);
                    SchedulingUtils.recordTeachingHours(lecturerHourMap, tutor, 2);
                }

                List<Student> groupStudents = allStudentsSorted; // NOT slice, assign full list to all groups

                metaDataList.add(new SessionGroupMetaData(
                    0,  // semester not relevant due to merged input
                    module.getId(),
                    typeInfo.type(),
                    groupName,
                    tutor,
                    MAX_GROUP_SIZE,
                    i,
                    groupCount,
                    groupStudents
                ));
            }
        }

        return metaDataList;
    }

    private static record SessionTypeInfo(String type, boolean hasType, List<String> tutor) {}

    public List<Integer> getSemestersForModule(String moduleId) {
        return new ArrayList<>(moduleSemesterMap.getOrDefault(moduleId, Set.of()));
    }

    public void reset() {
        lecturerHourMap.clear();
        moduleSemesterMap.clear(); 
    }
}

